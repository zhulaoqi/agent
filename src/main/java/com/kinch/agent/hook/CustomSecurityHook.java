package com.kinch.agent.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.JumpTo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * 安全检查Hook - 使用框架的Hook机制
 * 检测SQL注入、命令注入、敏感信息泄露等安全问题
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HookPositions({HookPosition.BEFORE_MODEL})
public class CustomSecurityHook extends ModelHook {

    // SQL注入检测模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        ".*(union|select|insert|update|delete|drop|exec|script|<script).*",
        Pattern.CASE_INSENSITIVE
    );

    // 命令注入检测模式
    private static final Pattern COMMAND_INJECTION_PATTERN = Pattern.compile(
        ".*[;&|`$].*",
        Pattern.CASE_INSENSITIVE
    );

    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
        "密码", "password", "token", "秘钥", "secret", "apikey"
    );

    @Override
    public String getName() {
        return "custom_security_hook";
    }

    @Override
    public List<JumpTo> canJumpTo() {
        // 支持跳转到结束（用于拦截不安全的请求）
        return List.of(JumpTo.end);
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        try {
            log.debug("🛡️ 安全Hook - 检查开始");

            // 获取消息列表
            Optional<Object> messagesOpt = state.value("messages");
            if (messagesOpt.isEmpty()) {
                return CompletableFuture.completedFuture(new HashMap<>());
            }

            @SuppressWarnings("unchecked")
            List<Message> messages = (List<Message>) messagesOpt.get();

            // 检查最后一条用户消息
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message msg = messages.get(i);
                if (msg.getText() != null) {
                    String content = msg.getText();
                    
                    // 执行安全检查
                    SecurityCheckResult result = checkSecurity(content);
                    
                    if (!result.isSafe()) {
                        log.warn("🚨 安全检查失败: {}", result.getMessage());
                        
                        // 添加安全警告消息并终止执行
                        List<Message> updatedMessages = new ArrayList<>(messages);
                        updatedMessages.add(new AssistantMessage(
                            "⚠️ 安全检查失败: " + result.getMessage() + "\n请修改您的输入后重试。"
                        ));
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("messages", updatedMessages);
                        // 注意：跳转控制需要配合RunnableConfig使用
                        
                        return CompletableFuture.completedFuture(updates);
                    }
                }
            }

            log.debug("✅ 安全检查通过");

        } catch (Exception e) {
            log.error("❌ 安全Hook执行失败", e);
        }

        return CompletableFuture.completedFuture(new HashMap<>());
    }

    /**
     * 执行安全检查
     */
    public SecurityCheckResult checkSecurity(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new SecurityCheckResult(true, "输入为空");
        }

        // 1. 检查SQL注入
        if (SQL_INJECTION_PATTERN.matcher(input).matches()) {
            return new SecurityCheckResult(false, "检测到可疑的SQL注入尝试");
        }

        // 2. 检查命令注入
        if (COMMAND_INJECTION_PATTERN.matcher(input).matches()) {
            return new SecurityCheckResult(false, "检测到可疑的命令注入尝试");
        }

        // 3. 检查敏感词（仅警告）
        for (String word : SENSITIVE_WORDS) {
            if (input.toLowerCase().contains(word.toLowerCase())) {
                log.warn("⚠️ 检测到敏感词: {}", word);
                // 敏感词不阻止执行，只记录日志
            }
        }

        return new SecurityCheckResult(true, "安全检查通过");
    }

    /**
     * 过滤敏感信息
     */
    public String filterSensitiveInfo(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String filtered = text;

        // 过滤手机号
        filtered = filtered.replaceAll("1[3-9]\\d{9}", "***********");

        // 过滤身份证号
        filtered = filtered.replaceAll("\\d{17}[\\dXx]", "******************");

        // 过滤邮箱
        filtered = filtered.replaceAll("[\\w.-]+@[\\w.-]+\\.\\w+", "***@***.***");

        // 过滤IP地址
        filtered = filtered.replaceAll("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", "***.***.***.***");

        return filtered;
    }

    /**
     * 安全检查结果
     */
    @Data
    public static class SecurityCheckResult {
        private final boolean safe;
        private final String message;

        public SecurityCheckResult(boolean safe, String message) {
            this.safe = safe;
            this.message = message;
        }
    }
}



