package com.kinch.agent.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.kinch.agent.workflow.RealStateGraphWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * StateGraph 工作流控制器
 * 真正使用Spring AI Alibaba Graph Core的StateGraph API
 * 
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/state-graph")
@RequiredArgsConstructor
public class StateGraphController {

    private final RealStateGraphWorkflow realStateGraphWorkflow;

    /**
     * 执行开发工作流
     * 
     * 使用真正的StateGraph，包含5个节点：
     * 需求分析 → 方案设计 → 代码生成 → 测试 → 完成
     */
    @PostMapping("/development")
    public Map<String, Object> executeDevelopmentWorkflow(@RequestBody Map<String, String> request) {
        String requirement = request.get("requirement");
        log.info("执行StateGraph开发工作流 - 需求: {}", requirement);

        Map<String, Object> response = new HashMap<>();
        
        try {
            // 创建StateGraph
            CompiledGraph graph = realStateGraphWorkflow.createDevelopmentWorkflow();
            
            // 准备输入
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("requirement", requirement);
            
            // 执行工作流
            Map<String, Object> result = realStateGraphWorkflow.executeWorkflow(graph, inputs);
            
            response.put("success", true);
            response.put("workflow_type", "stategraph");
            response.put("result", result);
            response.put("note", "这是真正的StateGraph实现，不是模拟的！");
            
        } catch (Exception e) {
            log.error("StateGraph工作流执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * 执行条件分支工作流
     * 
     * 使用StateGraph的条件边功能，根据分类结果选择不同分支
     */
    @PostMapping("/conditional")
    public Map<String, Object> executeConditionalWorkflow(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        log.info("执行StateGraph条件工作流 - 输入: {}", input);

        Map<String, Object> response = new HashMap<>();
        
        try {
            // 创建条件分支StateGraph
            CompiledGraph graph = realStateGraphWorkflow.createConditionalWorkflow();
            
            // 准备输入
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("input", input);
            
            // 执行工作流
            Map<String, Object> result = realStateGraphWorkflow.executeWorkflow(graph, inputs);
            
            response.put("success", true);
            response.put("workflow_type", "conditional_stategraph");
            response.put("result", result);
            response.put("note", "使用了StateGraph的条件边（ConditionalEdge）功能");
            
        } catch (Exception e) {
            log.error("条件工作流执行失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取StateGraph说明
     */
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("framework", "Spring AI Alibaba Graph Core");
        info.put("api", "StateGraph");
        info.put("features", new String[]{
            "节点定义（addNode）",
            "边定义（addEdge）",
            "条件边（addConditionalEdge）",
            "状态管理（OverAllState）",
            "工作流编译和执行"
        });
        info.put("status", "真实实现（非模拟）");
        info.put("endpoints", new String[]{
            "POST /api/state-graph/development - 开发工作流",
            "POST /api/state-graph/conditional - 条件分支工作流"
        });
        return info;
    }
}



