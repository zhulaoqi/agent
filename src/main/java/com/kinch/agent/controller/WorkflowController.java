package com.kinch.agent.controller;

import com.kinch.agent.workflow.RealStateGraphWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 工作流控制器
 * 
 * 提供简洁的工作流API
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final RealStateGraphWorkflow workflowService;

    /**
     * 执行开发工作流
     * 
     * 完整流程：需求分析 → 方案设计 → 代码生成 → 测试
     */
    @PostMapping("/sequential")
    public Map<String, Object> executeSequentialWorkflow(@RequestBody Map<String, String> request) {
        String requirement = request.get("requirement");
        log.info("执行开发工作流 - 需求: {}", requirement);

        Map<String, Object> response = new HashMap<>();
        try {
            Object graph = workflowService.createDevelopmentWorkflow();
            
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("requirement", requirement);
            
            Map<String, Object> result = workflowService.executeWorkflow(graph, inputs);
            
            response.put("success", true);
            response.put("result", result);
            response.put("type", "sequential");

        } catch (Exception e) {
            log.error("工作流执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * 执行条件分支工作流（真正的 StateGraph）
     * 
     * 自动识别输入类型并智能路由：
     * - technical（技术问题） → 技术专家处理
     * - business（业务需求） → 业务分析师处理
     * - general（一般问题） → 通用助手处理
     */
    @PostMapping("/conditional")
    public Map<String, Object> executeConditionalWorkflow(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        log.info("执行条件工作流 - 输入: {}", input);

        Map<String, Object> response = new HashMap<>();
        try {
            // 使用真正的 StateGraph 条件分支工作流
            Map<String, Object> result = workflowService.executeConditionalWorkflow(input);
            
            response.put("success", true);
            response.put("result", result);
            response.put("type", "conditional_stategraph");
            response.put("note", "使用StateGraph条件边（addConditionalEdges）实现智能路由");

        } catch (Exception e) {
            log.error("条件工作流执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * 执行开发工作流（兼容旧接口）
     */
    @PostMapping("/dev")
    public Map<String, Object> executeDevelopmentWorkflow(@RequestBody Map<String, String> request) {
        return executeSequentialWorkflow(request);
    }

    /**
     * 获取工作流信息
     */
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("framework", "Spring AI Alibaba StateGraph");
        info.put("implementation", "真正的Graph Core实现");
        info.put("available_workflows", List.of(
            "开发工作流（8个节点，智能路由）",
            "条件分支工作流（4个节点，自动分类）"
        ));
        info.put("endpoints", Map.of(
            "POST /api/workflow/sequential", "开发工作流：需求分类→分析→设计→代码→测试",
            "POST /api/workflow/conditional", "条件工作流：自动分类→专家处理（technical/business/general）",
            "POST /api/workflow/dev", "开发工作流（兼容旧接口）"
        ));
        info.put("features", List.of(
            "StateGraph API",
            "条件边动态路由",
            "状态持久化",
            "人工介入支持"
        ));
        return info;
    }
}
