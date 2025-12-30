package com.kinch.agent.controller;

import com.kinch.agent.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG控制器
 * 处理知识库和检索增强生成请求
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    /**
     * 添加文档到知识库
     */
    @PostMapping("/document/add")
    public Map<String, Object> addDocument(@RequestBody Map<String, String> request) {
        String knowledgeBaseId = request.get("knowledgeBaseId");
        String title = request.get("title");
        String content = request.get("content");

        log.info("添加文档 - KB: {}, 标题: {}", knowledgeBaseId, title);

        Map<String, Object> response = new HashMap<>();
        try {
            String result = ragService.addDocument(knowledgeBaseId, title, content);
            response.put("success", true);
            response.put("message", result);
        } catch (Exception e) {
            log.error("添加文档失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * RAG问答
     */
    @PostMapping("/query")
    public Map<String, Object> query(@RequestBody Map<String, String> request) {
        String knowledgeBaseId = request.get("knowledgeBaseId");
        String question = request.get("question");

        log.info("RAG问答 - KB: {}, 问题: {}", knowledgeBaseId, question);

        Map<String, Object> response = new HashMap<>();
        try {
            String answer = ragService.query(knowledgeBaseId, question);
            response.put("success", true);
            response.put("answer", answer);
            response.put("question", question);
        } catch (Exception e) {
            log.error("RAG问答失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 列出所有知识库
     */
    @GetMapping("/knowledge-bases")
    public Map<String, Object> listKnowledgeBases() {
        log.info("获取知识库列表");

        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> knowledgeBases = ragService.listKnowledgeBases();
            response.put("success", true);
            response.put("data", knowledgeBases);
        } catch (Exception e) {
            log.error("获取知识库列表失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
}



