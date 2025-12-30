package com.kinch.agent.service;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.kinch.agent.config.AgentConfig;
import com.kinch.agent.entity.Conversation;
import com.kinch.agent.entity.Message;
import com.kinch.agent.entity.User;
import com.kinch.agent.repository.ConversationRepository;
import com.kinch.agent.repository.MessageRepository;
import com.kinch.agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Agent服务 - 核心对话服务
 * 使用Spring AI Alibaba的ReactAgent
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentConfig.AgentFactory agentFactory;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * 对话接口 - 核心方法
     * 
     * @param userId 用户ID
     * @param userMessage 用户消息
     * @param conversationId 会话ID（可选）
     * @param ipAddress IP地址
     * @return 助手回复消息
     */
    @Transactional(rollbackFor = Exception.class)
    public Message chat(Long userId, String userMessage, Long conversationId, String ipAddress) {
        log.info("用户对话 - userId: {}, message: {}", userId, userMessage);

        // 1. 获取用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 获取或创建会话
        Conversation conversation = getOrCreateConversation(userId, conversationId);

        // 3. 保存用户消息
        Message userMsg = saveMessage(conversation.getId(), userId, "user", userMessage);

        // 4. 构建RunnableConfig - 传入元数据供Hooks使用
        RunnableConfig config = RunnableConfig.builder()
                .threadId(conversation.getThreadId())  // 用于记忆持久化
                .addMetadata("userId", userId)
                .addMetadata("username", user.getUsername())
                .addMetadata("conversationId", conversation.getId())
                .addMetadata("ipAddress", ipAddress != null ? ipAddress : "")
                .build();

        try {
            // 5. 构建并调用ReactAgent
            ReactAgent agent = agentFactory.createAgent("chat_agent");

            // 调用Agent（会触发Hooks: onAgentStart -> onToolStart/End -> onAgentEnd）
            AssistantMessage response = agent.call(userMessage, config);

            // 6. 保存助手消息
            String assistantReply = response.getText();
            Message assistantMsg = saveMessage(
                    conversation.getId(),
                    userId,
                    "assistant",
                    assistantReply
            );

            // 7. 更新会话统计
            updateConversationStats(conversation);

            log.info("对话完成 - userId: {}, conversationId: {}", userId, conversation.getId());
            return assistantMsg;

        } catch (Exception e) {
            log.error("Agent调用失败 - userId: {}, error: {}", userId, e.getMessage(), e);
            
            // 保存错误消息
            Message errorMsg = saveMessage(
                    conversation.getId(),
                    userId,
                    "assistant",
                    "抱歉，处理您的请求时出现错误：" + e.getMessage()
            );
            
            throw new RuntimeException("AI服务异常：" + e.getMessage());
        }
    }

    /**
     * 获取或创建会话
     */
    private Conversation getOrCreateConversation(Long userId, Long conversationId) {
        if (conversationId != null) {
            return conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("会话不存在"));
        }

        // 创建新会话
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setThreadId(UUID.randomUUID().toString());
        conversation.setTitle("新对话");
        conversation.setType("chat");
        conversation.setAgentName("chat_agent");
        conversation.setStatus("active");
        
        return conversationRepository.save(conversation);
    }

    /**
     * 保存消息
     */
    private Message saveMessage(Long conversationId, Long userId, String role, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setType("text");
        
        return messageRepository.save(message);
    }

    /**
     * 更新会话统计
     */
    private void updateConversationStats(Conversation conversation) {
        conversation.setMessageCount(conversation.getMessageCount() + 2);
        
        // 如果是新会话，根据第一条消息生成标题
        if ("新对话".equals(conversation.getTitle())) {
            java.util.List<Message> messages = messageRepository
                    .findByConversationIdOrderByCreateTimeAsc(conversation.getId());
            if (!messages.isEmpty()) {
                String firstMessage = messages.get(0).getContent();
                String title = firstMessage.length() > 20 
                        ? firstMessage.substring(0, 20) + "..." 
                        : firstMessage;
                conversation.setTitle(title);
            }
        }
        
        conversationRepository.save(conversation);
    }

    /**
     * 获取会话历史
     */
    public java.util.List<Message> getConversationHistory(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreateTimeAsc(conversationId);
    }

    /**
     * 获取用户的所有会话
     */
    public java.util.List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdAndStatusOrderByUpdateTimeDesc(userId, "active");
    }

    /**
     * 删除会话
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除该会话");
        }

        conversation.setStatus("deleted");
        conversationRepository.save(conversation);
    }
}

