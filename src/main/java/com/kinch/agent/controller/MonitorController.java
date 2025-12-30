package com.kinch.agent.controller;

import com.kinch.agent.hook.*;
import com.kinch.agent.interceptor.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 监控控制器
 * 展示Hooks和Interceptors的监控数据
 * 
 * @author kinch
 * @date 2025-12-30
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final CustomAuditHook auditHook;
    private final CustomSecurityHook securityHook;
    private final CustomTokenLimitHook tokenLimitHook;
    private final PerformanceInterceptor performanceInterceptor;
    private final SafetyInterceptor safetyInterceptor;
    private final ToolMonitorInterceptor toolMonitorInterceptor;

    /**
     * 获取审计日志
     */
    @GetMapping("/audit")
    public Map<String, Object> getAuditLogs() {
        log.info("获取审计日志");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", auditHook.getRecentLogs());
        response.put("note", "CustomAuditHook记录所有模型调用");
        return response;
    }

    /**
     * 测试安全检查
     */
    @PostMapping("/security/check")
    public Map<String, Object> checkSecurity(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        log.info("安全检查 - 输入: {}", input);
        
        CustomSecurityHook.SecurityCheckResult result = securityHook.checkSecurity(input);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("safe", result.isSafe());
        response.put("message", result.getMessage());
        response.put("filtered", securityHook.filterSensitiveInfo(input));
        response.put("note", "检测SQL注入、命令注入、敏感信息");
        return response;
    }

    /**
     * 获取Token使用情况
     */
    @GetMapping("/token/{userId}")
    public Map<String, Object> getTokenUsage(@PathVariable Long userId) {
        log.info("查询Token使用 - 用户: {}", userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("remaining", tokenLimitHook.getRemainingQuota(userId));
        response.put("canProceed", tokenLimitHook.checkLimit(userId, 1000));
        response.put("note", "自动限流、配额管理");
        return response;
    }

    /**
     * 获取性能统计
     */
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceStats() {
        log.info("获取性能统计");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", performanceInterceptor.getStatistics());
        response.put("note", "PerformanceInterceptor监控模型调用性能");
        return response;
    }

    /**
     * 获取安全拦截统计
     */
    @GetMapping("/safety")
    public Map<String, Object> getSafetyStats() {
        log.info("获取安全统计");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", safetyInterceptor.getStatistics());
        response.put("note", "SafetyInterceptor进行内容审核");
        return response;
    }

    /**
     * 获取工具调用统计
     */
    @GetMapping("/tools")
    public Map<String, Object> getToolStats() {
        log.info("获取工具统计");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", toolMonitorInterceptor.getStatistics());
        response.put("note", "ToolMonitorInterceptor监控所有工具调用");
        return response;
    }

    /**
     * 获取完整监控概览
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        log.info("获取监控概览");
        
        Map<String, Object> overview = new HashMap<>();
        
        // Hooks
        Map<String, Object> hooks = new HashMap<>();
        hooks.put("audit", Map.of(
            "name", "审计日志Hook",
            "type", "ModelHook",
            "logCount", auditHook.getRecentLogs().size()
        ));
        hooks.put("security", Map.of(
            "name", "安全检查Hook",
            "type", "ModelHook",
            "status", "active"
        ));
        hooks.put("tokenLimit", Map.of(
            "name", "Token限流Hook",
            "type", "ModelHook",
            "status", "active"
        ));
        hooks.put("messageTrimming", Map.of(
            "name", "消息修剪Hook",
            "type", "MessagesModelHook",
            "status", "active"
        ));
        
        // Interceptors
        Map<String, Object> interceptors = new HashMap<>();
        interceptors.put("performance", Map.of(
            "name", "性能监控",
            "type", "ModelInterceptor",
            "stats", performanceInterceptor.getStatistics()
        ));
        interceptors.put("safety", Map.of(
            "name", "安全拦截",
            "type", "ModelInterceptor",
            "stats", safetyInterceptor.getStatistics()
        ));
        interceptors.put("toolMonitor", Map.of(
            "name", "工具监控",
            "type", "ToolInterceptor",
            "stats", toolMonitorInterceptor.getStatistics()
        ));
        
        overview.put("success", true);
        overview.put("hooks", hooks);
        overview.put("interceptors", interceptors);
        overview.put("framework", "Spring AI Alibaba");
        overview.put("note", "完整的Hooks和Interceptors生态");
        
        return overview;
    }
}

