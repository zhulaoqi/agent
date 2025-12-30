package com.kinch.agent.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 性能监控Interceptor - 使用框架的ModelInterceptor
 * 监控模型调用性能和Token使用情况
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
public class PerformanceInterceptor extends ModelInterceptor {

    private long totalCalls = 0;
    private long totalTime = 0;
    private long maxTime = 0;
    private long minTime = Long.MAX_VALUE;

    @Override
    public String getName() {
        return "performance_interceptor";
    }
    
    /**
     * 获取统计信息
     */
    public synchronized Map<String, Object> getStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCalls", totalCalls);
        stats.put("totalTime", totalTime);
        stats.put("avgTime", totalCalls > 0 ? totalTime / totalCalls : 0);
        stats.put("maxTime", maxTime);
        stats.put("minTime", minTime == Long.MAX_VALUE ? 0 : minTime);
        return stats;
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        long startTime = System.currentTimeMillis();
        int messageCount = request.getMessages().size();

        log.info("🚀 模型调用开始 - 消息数: {}", messageCount);

        try {
            // 执行实际的模型调用
            ModelResponse response = handler.call(request);

            long duration = System.currentTimeMillis() - startTime;
            
            // 更新统计
            synchronized (this) {
                totalCalls++;
                totalTime += duration;
                maxTime = Math.max(maxTime, duration);
                minTime = Math.min(minTime, duration);
            }
            
            // 记录性能指标
            log.info("✅ 模型调用成功 - 耗时: {}ms, 消息: {}", duration, messageCount);

            // 添加性能指标到响应（如果需要）
            if (duration > 5000) {
                log.warn("⚠️ 模型调用较慢: {}ms", duration);
            }

            return response;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ 模型调用失败 - 耗时: {}ms, 错误: {}", duration, e.getMessage());
            throw e;
        }
    }
}


