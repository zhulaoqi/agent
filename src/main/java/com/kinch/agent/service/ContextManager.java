package com.kinch.agent.service;

import com.kinch.agent.entity.Conversation;
import com.kinch.agent.entity.Message;
import com.kinch.agent.repository.ConversationRepository;
import com.kinch.agent.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 上下文管理器 - Memory Management
 * 实现对话历史的持久化和上下文窗口管理
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextManager {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    // 默认上下文窗口大小
    private static final int DEFAULT_CONTEXT_WINDOW = 10;
    // 最大上下文窗口
    private static final int MAX_CONTEXT_WINDOW = 50;

    /**
     * 获取对话历史上下文
     *
     * @param conversationId 对话ID
     * @param windowSize 窗口大小（最近N轮对话）
     * @return Spring AI的Message列表
     */
    public List<org.springframework.ai.chat.messages.Message> getContext(Long conversationId, Integer windowSize) {
        log.info("获取上下文 - 对话ID: {}, 窗口大小: {}", conversationId, windowSize);

        int actualWindowSize = windowSize != null ? 
                Math.min(windowSize, MAX_CONTEXT_WINDOW) : DEFAULT_CONTEXT_WINDOW;

        // 获取最近的消息
        List<Message> messages = messageRepository
                .findByConversationIdOrderByCreateTimeAsc(conversationId)
                .stream()
                .limit(actualWindowSize * 2)  // 一轮对话包含用户和助手两条消息
                .collect(Collectors.toList());

        // 转换为Spring AI的Message格式
        List<org.springframework.ai.chat.messages.Message> aiMessages = new ArrayList<>();
        
        // 添加系统提示
        aiMessages.add(new SystemMessage("你是一个智能AI助手，可以帮助用户完成各种任务。"));

        // 按时间顺序排列（从旧到新）
        messages.stream()
                .sorted(Comparator.comparing(Message::getCreateTime))
                .forEach(msg -> {
                    if ("user".equals(msg.getRole())) {
                        aiMessages.add(new UserMessage(msg.getContent()));
                    } else if ("assistant".equals(msg.getRole())) {
                        aiMessages.add(new AssistantMessage(msg.getContent()));
                    }
                });

        log.info("上下文消息数量: {}", aiMessages.size());
        return aiMessages;
    }

    /**
     * 保存消息到上下文
     */
    @Transactional
    public void saveMessage(Long conversationId, String role, String content, Integer tokenCount) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setTokenCount(tokenCount != null ? tokenCount : estimateTokens(content));
        message.setCreateTime(LocalDateTime.now());
        
        messageRepository.save(message);
        log.debug("保存消息 - 对话: {}, 角色: {}, Token: {}", conversationId, role, message.getTokenCount());
    }

    /**
     * 清理旧上下文
     * 保留最近N条消息，删除更早的消息以控制存储
     */
    @Transactional
    public int cleanOldContext(Long conversationId, int keepCount) {
        log.info("清理旧上下文 - 对话: {}, 保留数量: {}", conversationId, keepCount);

        List<Message> allMessages = messageRepository
                .findByConversationIdOrderByCreateTimeAsc(conversationId);

        if (allMessages.size() <= keepCount) {
            return 0;
        }

        List<Message> toDelete = allMessages.subList(keepCount, allMessages.size());
        messageRepository.deleteAll(toDelete);

        log.info("已删除{}条旧消息", toDelete.size());
        return toDelete.size();
    }

    /**
     * 获取上下文摘要（用于超长对话）
     */
    public String getContextSummary(Long conversationId) {
        log.info("生成上下文摘要 - 对话: {}", conversationId);

        List<Message> messages = messageRepository
                .findByConversationIdOrderByCreateTimeAsc(conversationId)
                .stream()
                .limit(20)
                .collect(Collectors.toList());

        if (messages.isEmpty()) {
            return "新对话";
        }

        // 统计信息
        long userMessages = messages.stream().filter(m -> "user".equals(m.getRole())).count();
        long assistantMessages = messages.stream().filter(m -> "assistant".equals(m.getRole())).count();
        int totalTokens = messages.stream()
                .mapToInt(m -> m.getTokenCount() != null ? m.getTokenCount() : 0)
                .sum();

        return String.format("对话轮次: %d, 用户消息: %d, 助手回复: %d, 总Token: %d",
                messages.size() / 2, userMessages, assistantMessages, totalTokens);
    }

    /**
     * 创建新对话
     */
    @Transactional
    public Conversation createConversation(Long userId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title != null ? title : "新对话");
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        
        return conversationRepository.save(conversation);
    }

    /**
     * 更新对话标题
     */
    @Transactional
    public void updateConversationTitle(Long conversationId, String newTitle) {
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setTitle(newTitle);
            conv.setUpdateTime(LocalDateTime.now());
            conversationRepository.save(conv);
        });
    }

    /**
     * 简单的Token估算（1个中文字符约等于2个token，1个英文单词约等于1.3个token）
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int chineseCount = text.replaceAll("[^\u4e00-\u9fa5]", "").length();
        int englishWordCount = text.split("\\s+").length;
        
        return (int) (chineseCount * 2 + englishWordCount * 1.3);
    }
}



