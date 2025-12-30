package com.kinch.agent.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.kinch.agent.entity.AuditLog;
import com.kinch.agent.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 审计日志Hook - 使用框架的Hook机制
 * 记录所有模型调用和工具调用
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HookPositions({HookPosition.BEFORE_MODEL, HookPosition.AFTER_MODEL})
public class CustomAuditHook extends ModelHook {

    private final AuditLogRepository auditLogRepository;
    
    private static final String START_TIME_KEY = "__audit_start_time__";
    private static final String MESSAGE_COUNT_KEY = "__audit_message_count__";

    @Override
    public String getName() {
        return "custom_audit_hook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        try {
            log.debug("🔍 审计Hook - 模型调用前");

            // 记录开始时间
            config.context().put(START_TIME_KEY, System.currentTimeMillis());

            // 获取消息列表
            Optional<Object> messagesOpt = state.value("messages");
            if (messagesOpt.isPresent()) {
                @SuppressWarnings("unchecked")
                List<Message> messages = (List<Message>) messagesOpt.get();
                config.context().put(MESSAGE_COUNT_KEY, messages.size());

                // 创建审计记录
                AuditLog auditLog = new AuditLog();
                auditLog.setUserId(1L); // TODO: 从context获取真实用户ID
                auditLog.setOperationType("model_call");
                auditLog.setAgentName("intelligent_agent"); // 使用固定名称
                auditLog.setInput(messages.size() + " messages");
                auditLog.setStatus("processing");

                auditLogRepository.save(auditLog);
                log.debug("✅ 审计记录已创建");
            }

        } catch (Exception e) {
            log.error("❌ 审计Hook执行失败", e);
        }

        return CompletableFuture.completedFuture(new HashMap<>());
    }

    /**
     * 获取最近的审计日志
     */
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop10ByOrderByCreateTimeDesc();
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        try {
            log.debug("🔍 审计Hook - 模型调用后");

            // 计算耗时
            Long startTime = (Long) config.context().get(START_TIME_KEY);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

            // 获取消息数量
            Integer messageCount = (Integer) config.context().get(MESSAGE_COUNT_KEY);

            // 创建完成审计记录
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(1L);
            auditLog.setOperationType("model_response");
            auditLog.setOutput("Response received");
            auditLog.setDurationMs(duration);
            auditLog.setStatus("success");
            auditLog.setTokenCost(messageCount != null ? messageCount * 100 : 0); // 估算

            auditLogRepository.save(auditLog);
            log.info("✅ 模型调用完成 - 耗时: {}ms", duration);

        } catch (Exception e) {
            log.error("❌ 审计Hook执行失败", e);
        }

        return CompletableFuture.completedFuture(new HashMap<>());
    }
}
