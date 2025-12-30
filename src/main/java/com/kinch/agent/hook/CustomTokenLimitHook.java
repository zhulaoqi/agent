package com.kinch.agent.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.JumpTo;
import com.kinch.agent.entity.TokenUsageLog;
import com.kinch.agent.entity.User;
import com.kinch.agent.repository.TokenUsageLogRepository;
import com.kinch.agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Token限流Hook - 使用框架的Hook机制
 * 管理用户Token配额，防止过度消耗
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HookPositions({HookPosition.BEFORE_MODEL, HookPosition.AFTER_MODEL})
public class CustomTokenLimitHook extends ModelHook {

    private final UserRepository userRepository;
    private final TokenUsageLogRepository tokenUsageLogRepository;
    
    private static final String TOKEN_COUNT_KEY = "__token_count__";
    private static final String USER_ID_KEY = "__user_id__";
    private static final String START_TIME_KEY = "__token_start_time__";

    @Override
    public String getName() {
        return "custom_token_limit_hook";
    }

    @Override
    public List<JumpTo> canJumpTo() {
        // 支持跳转到结束（用于拦截超额请求）
        return List.of(JumpTo.end);
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        try {
            log.debug("💰 Token限流Hook - 检查开始");

            // 从context获取用户ID
            Long userId = getUserIdFromContext(config);
            config.context().put(USER_ID_KEY, userId);
            config.context().put(START_TIME_KEY, System.currentTimeMillis());

            // 检查用户Token配额
            if (!checkLimit(userId, 0)) { // 0表示只检查，不扣除
                log.warn("⚠️ 用户{}的Token配额不足", userId);
                
                // 添加配额不足消息并终止执行
                Optional<Object> messagesOpt = state.value("messages");
                if (messagesOpt.isPresent()) {
                    @SuppressWarnings("unchecked")
                    List<Message> messages = (List<Message>) messagesOpt.get();
                    List<Message> updatedMessages = new ArrayList<>(messages);
                    updatedMessages.add(new AssistantMessage(
                        "⚠️ Token配额不足，请联系管理员充值。\n当前剩余: " + getRemainingQuota(userId)
                    ));
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("messages", updatedMessages);
                    
                    return CompletableFuture.completedFuture(updates);
                }
            }

            // 估算本次请求的Token数量
            Optional<Object> messagesOpt = state.value("messages");
            if (messagesOpt.isPresent()) {
                @SuppressWarnings("unchecked")
                List<Message> messages = (List<Message>) messagesOpt.get();
                int estimatedTokens = estimateTokens(messages);
                config.context().put(TOKEN_COUNT_KEY, estimatedTokens);
                log.debug("📊 估算Token: {}", estimatedTokens);
            }

        } catch (Exception e) {
            log.error("❌ Token限流Hook执行失败", e);
        }

        return CompletableFuture.completedFuture(new HashMap<>());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        try {
            log.debug("💰 Token限流Hook - 记录使用");

            // 获取用户ID和Token数量
            Long userId = (Long) config.context().get(USER_ID_KEY);
            Integer estimatedTokens = (Integer) config.context().get(TOKEN_COUNT_KEY);
            Long startTime = (Long) config.context().get(START_TIME_KEY);
            
            if (userId == null || estimatedTokens == null) {
                return CompletableFuture.completedFuture(new HashMap<>());
            }

            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

            // 记录Token使用
            recordTokenUsage(userId, estimatedTokens, duration);
            
            // 扣除用户配额
            deductQuota(userId, estimatedTokens);

            log.info("✅ Token使用记录完成 - 用户: {}, Token: {}, 耗时: {}ms", 
                userId, estimatedTokens, duration);

        } catch (Exception e) {
            log.error("❌ Token记录失败", e);
        }

        return CompletableFuture.completedFuture(new HashMap<>());
    }

    /**
     * 从Context获取用户ID
     */
    private Long getUserIdFromContext(RunnableConfig config) {
        // TODO: 从RunnableConfig的metadata中获取用户ID
        // 这里暂时返回默认用户ID
        return 1L;
    }

    /**
     * 检查用户Token配额
     */
    public boolean checkLimit(Long userId, int requiredTokens) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            long remaining = user.getTokenQuota() - user.getTokenUsed();
            
            return remaining >= requiredTokens;
            
        } catch (Exception e) {
            log.error("检查Token配额失败", e);
            return true; // 失败时允许通过
        }
    }

    /**
     * 获取剩余配额
     */
    public long getRemainingQuota(Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return 0;
            }

            User user = userOpt.get();
            return user.getTokenQuota() - user.getTokenUsed();
            
        } catch (Exception e) {
            log.error("获取剩余配额失败", e);
            return 0;
        }
    }

    /**
     * 扣除用户配额
     */
    private void deductQuota(Long userId, int tokens) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return;
            }

            User user = userOpt.get();
            long newUsed = user.getTokenUsed() + tokens;
            user.setTokenUsed((int) newUsed);
            userRepository.save(user);
            
            log.debug("💸 扣除配额 - 用户: {}, Token: {}, 剩余: {}", 
                userId, tokens, user.getTokenQuota() - newUsed);
            
        } catch (Exception e) {
            log.error("扣除配额失败", e);
        }
    }

    /**
     * 记录Token使用
     */
    private void recordTokenUsage(Long userId, int tokens, long duration) {
        try {
            TokenUsageLog log = new TokenUsageLog();
            log.setUserId(userId);
            log.setModelName("qwen-plus");
            log.setInputTokens(tokens / 2);
            log.setOutputTokens(tokens / 2);
            log.setTotalTokens(tokens);
            log.setOperationType("chat");
            log.setEstimatedCost(tokens * 0.001); // 简单计费
            log.setDurationMs(duration);
            
            tokenUsageLogRepository.save(log);
            
        } catch (Exception e) {
            log.error("记录Token使用失败", e);
        }
    }

    /**
     * 估算Token数量
     */
    private int estimateTokens(List<Message> messages) {
        int total = 0;
        for (Message msg : messages) {
            if (msg.getText() != null) {
                // 简单估算：中文1字约1.5 token，英文1词约0.75 token
                String text = msg.getText();
                int chineseCount = text.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
                int otherCount = text.length() - chineseCount;
                total += (int) (chineseCount * 1.5 + otherCount * 0.25);
            }
        }
        return Math.max(100, total); // 最少100 token
    }
}

