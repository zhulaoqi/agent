package com.kinch.agent.service;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.kinch.agent.config.AgentConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多Agent协作服务
 * 支持监督者模式、并行执行、Agent间通信
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiAgentService {

    private final AgentConfig.AgentFactory agentFactory;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 监督者模式
     * 一个主Agent协调多个专家Agent
     */
    public Map<String, Object> supervisorPattern(String task) {
        log.info("监督者模式 - 任务: {}", task);

        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("pattern", "supervisor");

        try {
            // 1. 监督者分析任务
            ReactAgent supervisor = agentFactory.createAgent("supervisor");
            RunnableConfig config = RunnableConfig.builder().build();

            String analyzePrompt = String.format("""
                作为任务监督者，分析以下任务并决定需要哪些专家Agent：
                
                任务：%s
                
                请列出需要的专家类型（例如：研究员、开发者、测试员等）以及他们的任务分工。
                输出格式：专家名称|具体任务
                """, task);

            Message analysis = supervisor.call(new UserMessage(analyzePrompt), config);
            String plan = analysis.getText();
            result.put("plan", plan);

            // 2. 解析专家任务
            List<ExpertTask> expertTasks = parsePlan(plan);
            result.put("expertCount", expertTasks.size());

            // 3. 并行执行专家任务
            List<ExpertResult> expertResults = new ArrayList<>();
            for (ExpertTask expertTask : expertTasks) {
                ReactAgent expert = agentFactory.createAgent(expertTask.getExpertName());
                Message expertResponse = expert.call(new UserMessage(expertTask.getTask()), config);
                
                ExpertResult expertResult = new ExpertResult(
                    expertTask.getExpertName(),
                    expertTask.getTask(),
                    expertResponse.getText()
                );
                expertResults.add(expertResult);
                
                log.info("专家 {} 完成任务", expertTask.getExpertName());
            }

            result.put("expertResults", expertResults);

            // 4. 监督者整合结果
            StringBuilder consolidatePrompt = new StringBuilder();
            consolidatePrompt.append("作为监督者，整合以下专家的工作成果：\n\n");
            for (ExpertResult er : expertResults) {
                consolidatePrompt.append("【").append(er.getExpertName()).append("】\n");
                consolidatePrompt.append(er.getResult()).append("\n\n");
            }
            consolidatePrompt.append("请提供最终的综合报告。");

            Message finalResult = supervisor.call(new UserMessage(consolidatePrompt.toString()), config);
            result.put("finalResult", finalResult.getText());
            result.put("status", "success");

            log.info("监督者模式完成");

        } catch (Exception e) {
            log.error("监督者模式执行失败", e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 并行执行模式
     * 多个Agent同时处理不同任务
     */
    public Map<String, Object> parallelExecution(List<String> tasks) {
        log.info("并行执行模式 - 任务数: {}", tasks.size());

        Map<String, Object> result = new HashMap<>();
        result.put("taskCount", tasks.size());
        result.put("pattern", "parallel");

        try {
            // 并行执行任务
            List<CompletableFuture<TaskResult>> futures = new ArrayList<>();

            for (int i = 0; i < tasks.size(); i++) {
                final int taskIndex = i;
                final String task = tasks.get(i);

                CompletableFuture<TaskResult> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        String agentName = "agent_" + taskIndex;
                        ReactAgent agent = agentFactory.createAgent(agentName);
                        RunnableConfig config = RunnableConfig.builder().build();

                        long startTime = System.currentTimeMillis();
                        Message response = agent.call(new UserMessage(task), config);
                        long duration = System.currentTimeMillis() - startTime;

                        return new TaskResult(
                            agentName,
                            task,
                            response.getText(),
                            duration,
                            "success"
                        );

                    } catch (Exception e) {
                        log.error("任务执行失败: {}", task, e);
                        return new TaskResult(
                            "agent_" + taskIndex,
                            task,
                            "执行失败: " + e.getMessage(),
                            0,
                            "failed"
                        );
                    }
                }, executorService);

                futures.add(future);
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 收集结果
            List<TaskResult> results = new ArrayList<>();
            for (CompletableFuture<TaskResult> future : futures) {
                results.add(future.get());
            }

            result.put("results", results);
            result.put("status", "success");

            log.info("并行执行完成 - 成功: {}/{}", 
                results.stream().filter(r -> "success".equals(r.getStatus())).count(),
                results.size());

        } catch (Exception e) {
            log.error("并行执行失败", e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Agent间通信模式
     * Agent之间传递消息和协作
     */
    public Map<String, Object> agentCommunication(String initialMessage) {
        log.info("Agent间通信模式 - 初始消息: {}", initialMessage);

        Map<String, Object> result = new HashMap<>();
        result.put("initialMessage", initialMessage);
        result.put("pattern", "communication");

        try {
            List<CommunicationStep> steps = new ArrayList<>();

            // Agent A处理初始消息
            ReactAgent agentA = agentFactory.createAgent("agent_a");
            RunnableConfig config = RunnableConfig.builder().build();

            Message responseA = agentA.call(new UserMessage(initialMessage), config);
            steps.add(new CommunicationStep(
                "agent_a",
                "agent_b",
                initialMessage,
                responseA.getText()
            ));

            // Agent B基于A的输出继续处理
            ReactAgent agentB = agentFactory.createAgent("agent_b");
            String messageToB = "基于以下分析，请提供实施建议：\n\n" + responseA.getText();
            
            Message responseB = agentB.call(new UserMessage(messageToB), config);
            steps.add(new CommunicationStep(
                "agent_b",
                "agent_c",
                messageToB,
                responseB.getText()
            ));

            // Agent C进行最终总结
            ReactAgent agentC = agentFactory.createAgent("agent_c");
            String messageToC = "请对以下内容进行总结：\n\n" + responseB.getText();
            
            Message responseC = agentC.call(new UserMessage(messageToC), config);
            steps.add(new CommunicationStep(
                "agent_c",
                "end",
                messageToC,
                responseC.getText()
            ));

            result.put("steps", steps);
            result.put("finalResult", responseC.getText());
            result.put("status", "success");

            log.info("Agent间通信完成 - 步骤数: {}", steps.size());

        } catch (Exception e) {
            log.error("Agent通信失败", e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 解析监督者计划
     */
    private List<ExpertTask> parsePlan(String plan) {
        List<ExpertTask> tasks = new ArrayList<>();
        String[] lines = plan.split("\n");
        
        for (String line : lines) {
            if (line.contains("|")) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    tasks.add(new ExpertTask(
                        parts[0].trim(),
                        parts[1].trim()
                    ));
                }
            }
        }

        // 如果解析失败，添加默认专家
        if (tasks.isEmpty()) {
            tasks.add(new ExpertTask("通用专家", plan));
        }

        return tasks;
    }

    @Data
    public static class ExpertTask {
        private final String expertName;
        private final String task;
    }

    @Data
    public static class ExpertResult {
        private final String expertName;
        private final String task;
        private final String result;
    }

    @Data
    public static class TaskResult {
        private final String agentName;
        private final String task;
        private final String result;
        private final long duration;
        private final String status;
    }

    @Data
    public static class CommunicationStep {
        private final String fromAgent;
        private final String toAgent;
        private final String message;
        private final String response;
    }
}



