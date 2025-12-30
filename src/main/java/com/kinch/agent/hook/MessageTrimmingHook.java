package com.kinch.agent.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.messages.UpdatePolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息修剪Hook - 使用框架的MessagesModelHook
 * 控制上下文窗口大小，避免Token超限
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@HookPositions({HookPosition.BEFORE_MODEL})
public class MessageTrimmingHook extends MessagesModelHook {

    private static final int MAX_MESSAGES = 20; // 最多保留20条消息
    private static final int MIN_KEEP_MESSAGES = 5; // 至少保留5条最新消息

    @Override
    public String getName() {
        return "message_trimming_hook";
    }

    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        if (previousMessages == null || previousMessages.isEmpty()) {
            return new AgentCommand(previousMessages);
        }

        int messageCount = previousMessages.size();
        
        // 如果消息数量在限制内，直接返回
        if (messageCount <= MAX_MESSAGES) {
            log.debug("📊 消息数量: {} (未超限)", messageCount);
            return new AgentCommand(previousMessages);
        }

        log.info("✂️ 消息修剪 - 当前: {}, 限制: {}", messageCount, MAX_MESSAGES);

        // 保留系统消息和最新的N条消息
        List<Message> trimmedMessages = new ArrayList<>();
        
        // 1. 先添加系统消息
        for (Message msg : previousMessages) {
            if (msg instanceof SystemMessage) {
                trimmedMessages.add(msg);
            }
        }

        // 2. 添加最新的消息（保留最后N条）
        int keepCount = Math.max(MIN_KEEP_MESSAGES, MAX_MESSAGES - trimmedMessages.size());
        List<Message> recentMessages = previousMessages.subList(
            Math.max(0, messageCount - keepCount),
            messageCount
        );
        
        for (Message msg : recentMessages) {
            if (!(msg instanceof SystemMessage)) {
                trimmedMessages.add(msg);
            }
        }

        log.info("✅ 消息修剪完成 - 保留: {}/{}", trimmedMessages.size(), messageCount);
        
        // 使用REPLACE策略替换所有消息
        return new AgentCommand(trimmedMessages, UpdatePolicy.REPLACE);
    }
}



