package com.kinch.agent.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 工具监控Interceptor - 使用框架的ToolInterceptor
 * 监控工具调用性能和成功率
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
public class ToolMonitorInterceptor extends ToolInterceptor {

    private long totalCalls = 0;
    private long successCalls = 0;
    private long failedCalls = 0;

    @Override
    public String getName() {
        return "tool_monitor_interceptor";
    }
    
    public synchronized Map<String, Object> getStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCalls", totalCalls);
        stats.put("successCalls", successCalls);
        stats.put("failedCalls", failedCalls);
        stats.put("successRate", totalCalls > 0 ? (double)successCalls / totalCalls * 100 : 100);
        return stats;
    }

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        String toolName = request.getToolName();
        String args = request.getArguments();
        long startTime = System.currentTimeMillis();

        log.info("🔧 工具调用开始 - 工具: {}, 参数: {}", toolName, args);

        try {
            // 执行工具调用
            ToolCallResponse response = handler.call(request);

            long duration = System.currentTimeMillis() - startTime;
            synchronized (this) {
                totalCalls++;
                successCalls++;
            }
            log.info("✅ 工具调用成功 - 工具: {}, 耗时: {}ms", toolName, duration);

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            synchronized (this) {
                totalCalls++;
                failedCalls++;
            }
            log.error("❌ 工具调用失败 - 工具: {}, 耗时: {}ms, 错误: {}", 
                toolName, duration, e.getMessage());

            // 返回错误响应
            return ToolCallResponse.of(
                request.getToolCallId(),
                toolName,
                "工具执行失败: " + e.getMessage()
            );
        }
    }
}


