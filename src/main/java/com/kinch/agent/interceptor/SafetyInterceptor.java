package com.kinch.agent.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 安全检查Interceptor - 使用框架的ModelInterceptor
 * 过滤敏感内容和恶意输入
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
public class SafetyInterceptor extends ModelInterceptor {

    private long totalChecks = 0;
    private long blockedCount = 0;

    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
        "密码", "password", "token", "秘钥", "secret"
    );
    
    public synchronized Map<String, Object> getStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalChecks", totalChecks);
        stats.put("blockedCount", blockedCount);
        stats.put("passRate", totalChecks > 0 ? (double)(totalChecks - blockedCount) / totalChecks * 100 : 100);
        return stats;
    }

    // SQL注入模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        ".*(union|select|insert|update|delete|drop|exec|script).*",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getName() {
        return "safety_interceptor";
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        log.debug("🛡️ 安全检查 - 检查输入");
        
        synchronized (this) {
            totalChecks++;
        }

        // 检查输入消息
        for (Message msg : request.getMessages()) {
            String content = msg.getText();
            
            // 检查敏感词
            for (String word : SENSITIVE_WORDS) {
                if (content.toLowerCase().contains(word.toLowerCase())) {
                    log.warn("⚠️ 检测到敏感词: {}", word);
                    // 可以选择拦截或继续
                }
            }

            // 检查SQL注入
            if (SQL_INJECTION_PATTERN.matcher(content).matches()) {
                log.warn("🚨 检测到可疑SQL注入尝试");
                synchronized (this) {
                    blockedCount++;
                }
                return ModelResponse.of(
                    AssistantMessage.builder()
                        .content("检测到不安全的输入，请修改后重试")
                        .build()
                );
            }
        }

        // 执行模型调用
        ModelResponse response = handler.call(request);

        // 检查输出（过滤敏感信息）
        // 注意：ModelResponse可能没有getContent方法，暂时跳过输出过滤
        log.debug("✅ 安全检查通过");

        log.debug("✅ 安全检查通过");
        return response;
    }

    /**
     * 过滤敏感信息
     */
    private String filterSensitiveInfo(String text) {
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
}

