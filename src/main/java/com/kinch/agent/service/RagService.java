package com.kinch.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RAG检索增强生成服务
 * 支持文档检索和知识库问答
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatClient.Builder chatClientBuilder;
    
    // 简单的内存向量存储（生产环境应使用专业向量数据库）
    private final Map<String, DocumentChunk> vectorStore = new ConcurrentHashMap<>();
    private final Map<String, KnowledgeBase> knowledgeBases = new ConcurrentHashMap<>();

    /**
     * 添加文档到知识库
     */
    public String addDocument(String knowledgeBaseId, String title, String content) {
        log.info("添加文档到知识库 - KB: {}, 标题: {}", knowledgeBaseId, title);

        try {
            // 获取或创建知识库
            KnowledgeBase kb = knowledgeBases.computeIfAbsent(
                knowledgeBaseId,
                id -> new KnowledgeBase(id, "知识库-" + id)
            );

            // 分块
            List<String> chunks = chunkDocument(content);
            
            // 生成向量并存储
            for (int i = 0; i < chunks.size(); i++) {
                String chunkId = knowledgeBaseId + "_" + title + "_chunk_" + i;
                
                // 简化：使用字符串长度作为"向量"（生产环境应使用真实的embedding）
                float[] vector = generateSimpleVector(chunks.get(i));
                
                DocumentChunk chunk = new DocumentChunk(
                    chunkId, title, chunks.get(i), vector, knowledgeBaseId
                );
                
                vectorStore.put(chunkId, chunk);
                kb.addChunk(chunkId);
            }

            log.info("文档添加成功 - 共{}个分块", chunks.size());
            return "✅ 文档添加成功，共生成 " + chunks.size() + " 个分块";

        } catch (Exception e) {
            log.error("添加文档失败", e);
            return "❌ 添加失败: " + e.getMessage();
        }
    }

    /**
     * 检索相关文档
     */
    public List<DocumentChunk> retrieve(String knowledgeBaseId, String query, int topK) {
        log.info("检索文档 - KB: {}, 查询: {}, TopK: {}", knowledgeBaseId, query, topK);

        try {
            KnowledgeBase kb = knowledgeBases.get(knowledgeBaseId);
            if (kb == null) {
                return Collections.emptyList();
            }

            // 生成查询向量
            float[] queryVector = generateSimpleVector(query);

            // 计算相似度并排序
            List<ScoredChunk> scoredChunks = new ArrayList<>();
            for (String chunkId : kb.getChunkIds()) {
                DocumentChunk chunk = vectorStore.get(chunkId);
                if (chunk != null) {
                    float similarity = cosineSimilarity(queryVector, chunk.getVector());
                    scoredChunks.add(new ScoredChunk(chunk, similarity));
                }
            }

            scoredChunks.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));

            // 返回TopK
            return scoredChunks.stream()
                .limit(topK)
                .map(ScoredChunk::getChunk)
                .toList();

        } catch (Exception e) {
            log.error("检索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * RAG问答
     */
    public String query(String knowledgeBaseId, String question) {
        log.info("RAG问答 - KB: {}, 问题: {}", knowledgeBaseId, question);

        try {
            // 1. 检索相关文档
            List<DocumentChunk> relevantChunks = retrieve(knowledgeBaseId, question, 3);
            
            if (relevantChunks.isEmpty()) {
                return "抱歉，知识库中没有找到相关信息。";
            }

            // 2. 构建上下文
            StringBuilder context = new StringBuilder("参考资料：\n\n");
            for (int i = 0; i < relevantChunks.size(); i++) {
                context.append(i + 1).append(". ")
                       .append(relevantChunks.get(i).getContent())
                       .append("\n\n");
            }

            // 3. 生成答案
            String prompt = String.format("""
                请根据以下参考资料回答问题。如果参考资料中没有相关信息，请说明。
                
                %s
                
                问题：%s
                
                回答：
                """, context, question);

            ChatClient chatClient = chatClientBuilder.build();
            String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

            return answer;

        } catch (Exception e) {
            log.error("RAG问答失败", e);
            return "❌ 问答失败: " + e.getMessage();
        }
    }

    /**
     * 列出所有知识库
     */
    public List<Map<String, Object>> listKnowledgeBases() {
        List<Map<String, Object>> result = new ArrayList<>();
        knowledgeBases.values().forEach(kb -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", kb.getId());
            info.put("name", kb.getName());
            info.put("chunkCount", kb.getChunkIds().size());
            result.add(info);
        });
        return result;
    }

    /**
     * 文档分块
     */
    private List<String> chunkDocument(String content) {
        // 简单按段落分块（生产环境应使用更智能的分块策略）
        String[] paragraphs = content.split("\n\n+");
        List<String> chunks = new ArrayList<>();
        
        StringBuilder currentChunk = new StringBuilder();
        int maxChunkSize = 500;
        
        for (String para : paragraphs) {
            if (currentChunk.length() + para.length() > maxChunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(para).append("\n\n");
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }

    /**
     * 生成简单向量（模拟）
     * 生产环境应使用真实的Embedding模型
     */
    private float[] generateSimpleVector(String text) {
        // 简化：使用字符统计作为"向量"
        float[] vector = new float[10];
        for (int i = 0; i < vector.length; i++) {
            char c = (char) ('a' + i);
            vector[i] = text.toLowerCase().chars().filter(ch -> ch == c).count();
        }
        return normalize(vector);
    }

    /**
     * 向量归一化
     */
    private float[] normalize(float[] vector) {
        float sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        float magnitude = (float) Math.sqrt(sum);
        if (magnitude == 0) return vector;
        
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / magnitude;
        }
        return normalized;
    }

    /**
     * 余弦相似度
     */
    private float cosineSimilarity(float[] v1, float[] v2) {
        float dot = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
        }
        return dot;
    }

    /**
     * 文档分块
     */
    public static class DocumentChunk {
        private final String id;
        private final String title;
        private final String content;
        private final float[] vector;
        private final String knowledgeBaseId;

        public DocumentChunk(String id, String title, String content, float[] vector, String knowledgeBaseId) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.vector = vector;
            this.knowledgeBaseId = knowledgeBaseId;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public float[] getVector() { return vector; }
        public String getKnowledgeBaseId() { return knowledgeBaseId; }
    }

    /**
     * 知识库
     */
    public static class KnowledgeBase {
        private final String id;
        private final String name;
        private final Set<String> chunkIds = new HashSet<>();

        public KnowledgeBase(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public void addChunk(String chunkId) {
            chunkIds.add(chunkId);
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Set<String> getChunkIds() { return chunkIds; }
    }

    /**
     * 评分块
     */
    private static class ScoredChunk {
        private final DocumentChunk chunk;
        private final float score;

        public ScoredChunk(DocumentChunk chunk, float score) {
            this.chunk = chunk;
            this.score = score;
        }

        public DocumentChunk getChunk() { return chunk; }
        public float getScore() { return score; }
    }
}



