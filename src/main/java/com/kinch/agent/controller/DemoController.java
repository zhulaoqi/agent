package com.kinch.agent.controller;

import com.kinch.agent.dto.AnalysisRequest;
import com.kinch.agent.dto.StructuredWeatherResponse;
import com.kinch.agent.dto.TextAnalysisResult;
import com.kinch.agent.hook.CustomSecurityHook;
import com.kinch.agent.hook.CustomTokenLimitHook;
import com.kinch.agent.service.ContextManager;
import com.kinch.agent.service.StructuredOutputService;
import com.kinch.agent.workflow.RealStateGraphWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能演示控制器
 * 展示Spring AI Alibaba的核心能力
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final StructuredOutputService structuredOutputService;
    private final ContextManager contextManager;
    private final RealStateGraphWorkflow realStateGraphWorkflow;
    private final CustomSecurityHook customSecurityHook;
    private final CustomTokenLimitHook customTokenLimitHook;

    /**
     * 演示1：结构化输出 - 天气分析
     */
    @PostMapping("/structured/weather")
    public Map<String, Object> demoStructuredWeather(@RequestBody Map<String, String> request) {
        String city = request.get("city");
        String weatherData = request.getOrDefault("weatherData", "晴天，温度25度，湿度60%");
        
        log.info("演示结构化天气分析 - 城市: {}", city);
        
        try {
            StructuredWeatherResponse response = structuredOutputService.analyzeWeather(city, weatherData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            result.put("note", "这是Spring AI的结构化输出功能，自动将AI响应映射到Java对象");
            return result;
            
        } catch (Exception e) {
            log.error("结构化输出失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * 演示2：结构化输出 - 文本分析
     */
    @PostMapping("/structured/analysis")
    public Map<String, Object> demoStructuredAnalysis(@RequestBody AnalysisRequest request) {
        log.info("演示结构化文本分析");
        
        try {
            TextAnalysisResult result = structuredOutputService.analyzeText(request.getText());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("note", "自动提取情感、关键词、主题等结构化信息");
            return response;
            
        } catch (Exception e) {
            log.error("文本分析失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * 演示3：上下文管理
     */
    @GetMapping("/context/{conversationId}")
    public Map<String, Object> demoContext(@PathVariable Long conversationId) {
        log.info("演示上下文管理 - 对话ID: {}", conversationId);
        
        try {
            var messages = contextManager.getContext(conversationId, 5);
            String summary = contextManager.getContextSummary(conversationId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageCount", messages.size());
            result.put("summary", summary);
            result.put("note", "Memory Management: 自动管理对话历史，支持上下文窗口控制");
            return result;
            
        } catch (Exception e) {
            log.error("上下文查询失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * 演示4：StateGraph工作流编排（真实实现）
     */
    @PostMapping("/workflow/dev")
    public Map<String, Object> demoWorkflow(@RequestBody Map<String, String> request) {
        String requirement = request.get("requirement");
        log.info("演示StateGraph工作流 - 需求: {}", requirement);
        
        try {
            // 使用工作流
            Object graph = realStateGraphWorkflow.createDevelopmentWorkflow();
            
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("requirement", requirement);
            
            Map<String, Object> result = realStateGraphWorkflow.executeWorkflow(graph, inputs);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("note", "开发工作流：需求分析→方案设计→代码生成→测试");
            return response;
            
        } catch (Exception e) {
            log.error("工作流执行失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * 演示5：Token限流Hook
     */
    @GetMapping("/hook/token/{userId}")
    public Map<String, Object> demoTokenHook(@PathVariable Long userId) {
        log.info("演示Token限流 - 用户: {}", userId);
        
        try {
            long remaining = customTokenLimitHook.getRemainingQuota(userId);
            boolean canProceed = customTokenLimitHook.checkLimit(userId, 1000);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("remainingQuota", remaining);
            result.put("canProceed", canProceed);
            result.put("note", "Hook机制: 自动限流、审计、安全检查");
            return result;
            
        } catch (Exception e) {
            log.error("Token检查失败", e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * 演示6：安全检查Hook
     */
    @PostMapping("/hook/security")
    public Map<String, Object> demoSecurityHook(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        log.info("演示安全检查");
        
        CustomSecurityHook.SecurityCheckResult result = customSecurityHook.checkSecurity(input);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("safe", result.isSafe());
        response.put("message", result.getMessage());
        response.put("filtered", customSecurityHook.filterSensitiveInfo(input));
        response.put("note", "安全Hook: SQL注入、命令注入、敏感信息过滤");
        return response;
    }

    /**
     * 演示7：Hooks和Interceptors生态
     */
    @GetMapping("/hooks")
    public Map<String, Object> demoHooks() {
        log.info("演示Hooks和Interceptors生态");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("hooks", Map.of(
            "CustomAuditHook", "审计日志记录（ModelHook）",
            "CustomSecurityHook", "安全检查（ModelHook）",
            "CustomTokenLimitHook", "Token限流（ModelHook）",
            "MessageTrimmingHook", "消息自动修剪（MessagesModelHook）",
            "ModelCallLimitHook", "模型调用次数限制（框架内置）"
        ));
        result.put("interceptors", Map.of(
            "PerformanceInterceptor", "性能监控（ModelInterceptor）",
            "SafetyInterceptor", "安全检查（ModelInterceptor）",
            "ToolMonitorInterceptor", "工具监控（ToolInterceptor）"
        ));
        result.put("note", "完整的Hooks和Interceptors生态，覆盖Agent执行全生命周期");
        return result;
    }

    /**
     * 演示6：工具生态
     */
    @GetMapping("/tools")
    public Map<String, Object> demoTools() {
        log.info("演示工具生态");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tools", Map.of(
            "WeatherTool", "高德地图天气API集成",
            "NamingTool", "中文翻译+多格式命名生成",
            "ScriptGenerationTool", "运维脚本生成（Shell/Python）"
        ));
        result.put("note", "完整的工具生态，支持外部系统集成和复杂业务逻辑");
        return result;
    }

    /**
     * 功能清单
     */
    @GetMapping("/features")
    public Map<String, Object> listFeatures() {
        Map<String, Object> features = new HashMap<>();
        
        features.put("1_ReactAgent", "智能Agent，支持工具调用和多轮对话");
        features.put("2_Tools", "天气查询、命名助手、脚本生成等工具");
        features.put("3_StructuredOutput", "结构化输出，自动映射到Java对象");
        features.put("4_ContextManagement", "上下文管理，支持Memory和窗口控制");
        features.put("5_GraphCore", "工作流编排，支持复杂流程和条件分支");
        features.put("6_Streaming", "流式响应，实时输出（SSE）");
        features.put("7_Hooks_Interceptors", "完整的Hooks和Interceptors生态（审计、修剪、安全、监控）");
        features.put("8_Workflow", "Graph Core工作流编排（开发流程、并行执行）");
        features.put("9_Streaming", "SSE流式响应，实时AI输出");
        features.put("10_Frontend", "Vue3一体化部署，现代化UI");
        
        Map<String, Object> result = new HashMap<>();
        result.put("framework", "Spring AI Alibaba");
        result.put("features", features);
        result.put("status", "All core capabilities implemented!");
        
        return result;
    }
}

