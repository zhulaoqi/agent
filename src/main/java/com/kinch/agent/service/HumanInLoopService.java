package com.kinch.agent.service;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.kinch.agent.config.AgentConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Human-in-the-Loop服务
 * 支持人工介入和审批工作流
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HumanInLoopService {

    private final AgentConfig.AgentFactory agentFactory;
    
    // 存储待审批的工作流
    private final Map<String, PendingWorkflow> pendingWorkflows = new ConcurrentHashMap<>();

    /**
     * 创建需要人工审批的工作流
     */
    public Map<String, Object> createWorkflowWithApproval(String task) {
        log.info("创建人工介入工作流 - 任务: {}", task);

        String workflowId = UUID.randomUUID().toString();
        Map<String, Object> result = new HashMap<>();
        result.put("workflowId", workflowId);
        result.put("task", task);

        try {
            // 1. Agent生成初步方案
            ReactAgent agent = agentFactory.createAgent("proposal_agent");
            RunnableConfig config = RunnableConfig.builder().build();

            String proposalPrompt = String.format("""
                请为以下任务制定详细的执行方案：
                
                任务：%s
                
                方案应包括：
                1. 执行步骤
                2. 预期结果
                3. 潜在风险
                4. 所需资源
                """, task);

            Message response = agent.call(new UserMessage(proposalPrompt), config);
            String proposal = response.getText();

            // 2. 创建待审批工作流
            PendingWorkflow workflow = new PendingWorkflow(
                workflowId,
                task,
                proposal,
                "pending",
                new Date()
            );
            pendingWorkflows.put(workflowId, workflow);

            result.put("proposal", proposal);
            result.put("status", "pending");
            result.put("message", "方案已生成，等待人工审批");

            log.info("工作流创建成功 - ID: {}", workflowId);

        } catch (Exception e) {
            log.error("创建工作流失败", e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 审批工作流
     */
    public Map<String, Object> approveWorkflow(String workflowId, boolean approved, String feedback) {
        log.info("审批工作流 - ID: {}, 批准: {}", workflowId, approved);

        Map<String, Object> result = new HashMap<>();
        result.put("workflowId", workflowId);

        PendingWorkflow workflow = pendingWorkflows.get(workflowId);
        if (workflow == null) {
            result.put("status", "not_found");
            result.put("message", "工作流不存在");
            return result;
        }

        if (approved) {
            // 批准：执行工作流
            workflow.setStatus("approved");
            workflow.setFeedback(feedback);
            workflow.setApprovedAt(new Date());

            try {
                // 执行方案
                ReactAgent agent = agentFactory.createAgent("execution_agent");
                RunnableConfig config = RunnableConfig.builder().build();

                String executionPrompt = String.format("""
                    根据以下批准的方案执行任务：
                    
                    原任务：%s
                    
                    方案：%s
                    
                    审批意见：%s
                    
                    请执行并报告结果。
                    """, workflow.getTask(), workflow.getProposal(), feedback);

                Message response = agent.call(new UserMessage(executionPrompt), config);
                String executionResult = response.getText();

                workflow.setExecutionResult(executionResult);
                workflow.setStatus("completed");

                result.put("status", "completed");
                result.put("executionResult", executionResult);
                result.put("message", "工作流已批准并执行完成");

                log.info("工作流执行完成 - ID: {}", workflowId);

            } catch (Exception e) {
                log.error("工作流执行失败", e);
                workflow.setStatus("failed");
                result.put("status", "failed");
                result.put("error", e.getMessage());
            }

        } else {
            // 拒绝：记录反馈
            workflow.setStatus("rejected");
            workflow.setFeedback(feedback);
            workflow.setRejectedAt(new Date());

            result.put("status", "rejected");
            result.put("message", "工作流已拒绝");

            log.info("工作流已拒绝 - ID: {}", workflowId);
        }

        return result;
    }

    /**
     * 获取待审批列表
     */
    public List<Map<String, Object>> getPendingWorkflows() {
        List<Map<String, Object>> result = new ArrayList<>();

        pendingWorkflows.values().stream()
            .filter(w -> "pending".equals(w.getStatus()))
            .forEach(workflow -> {
                Map<String, Object> info = new HashMap<>();
                info.put("workflowId", workflow.getWorkflowId());
                info.put("task", workflow.getTask());
                info.put("proposal", workflow.getProposal());
                info.put("createdAt", workflow.getCreatedAt());
                result.add(info);
            });

        return result;
    }

    /**
     * 获取工作流详情
     */
    public Map<String, Object> getWorkflowDetail(String workflowId) {
        PendingWorkflow workflow = pendingWorkflows.get(workflowId);
        if (workflow == null) {
            return Map.of("status", "not_found");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("workflowId", workflow.getWorkflowId());
        detail.put("task", workflow.getTask());
        detail.put("proposal", workflow.getProposal());
        detail.put("status", workflow.getStatus());
        detail.put("feedback", workflow.getFeedback());
        detail.put("executionResult", workflow.getExecutionResult());
        detail.put("createdAt", workflow.getCreatedAt());
        detail.put("approvedAt", workflow.getApprovedAt());
        detail.put("rejectedAt", workflow.getRejectedAt());

        return detail;
    }

    @Data
    public static class PendingWorkflow {
        private final String workflowId;
        private final String task;
        private final String proposal;
        private String status; // pending, approved, rejected, completed, failed
        private final Date createdAt;
        private String feedback;
        private String executionResult;
        private Date approvedAt;
        private Date rejectedAt;

        public PendingWorkflow(String workflowId, String task, String proposal, String status, Date createdAt) {
            this.workflowId = workflowId;
            this.task = task;
            this.proposal = proposal;
            this.status = status;
            this.createdAt = createdAt;
        }
    }
}


