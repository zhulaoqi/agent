package com.kinch.agent.controller;

import com.kinch.agent.service.HumanInLoopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Human-in-the-Loop控制器
 * 处理人工介入工作流请求
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/human-in-loop")
@RequiredArgsConstructor
public class HumanInLoopController {

    private final HumanInLoopService humanInLoopService;

    /**
     * 创建需要审批的工作流
     */
    @PostMapping("/create")
    public Map<String, Object> createWorkflow(@RequestBody Map<String, String> request) {
        String task = request.get("task");
        log.info("创建人工介入工作流 - 任务: {}", task);

        try {
            return humanInLoopService.createWorkflowWithApproval(task);
        } catch (Exception e) {
            log.error("创建工作流失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 审批工作流
     */
    @PostMapping("/approve/{workflowId}")
    public Map<String, Object> approveWorkflow(
            @PathVariable String workflowId,
            @RequestBody Map<String, Object> request) {
        
        Boolean approved = (Boolean) request.get("approved");
        String feedback = (String) request.get("feedback");

        log.info("审批工作流 - ID: {}, 批准: {}", workflowId, approved);

        try {
            return humanInLoopService.approveWorkflow(workflowId, approved, feedback);
        } catch (Exception e) {
            log.error("审批失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 获取待审批列表
     */
    @GetMapping("/pending")
    public Map<String, Object> getPending() {
        log.info("获取待审批工作流列表");

        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> pending = humanInLoopService.getPendingWorkflows();
            response.put("success", true);
            response.put("data", pending);
        } catch (Exception e) {
            log.error("获取列表失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 获取工作流详情
     */
    @GetMapping("/detail/{workflowId}")
    public Map<String, Object> getDetail(@PathVariable String workflowId) {
        log.info("获取工作流详情 - ID: {}", workflowId);

        try {
            return humanInLoopService.getWorkflowDetail(workflowId);
        } catch (Exception e) {
            log.error("获取详情失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }
}



