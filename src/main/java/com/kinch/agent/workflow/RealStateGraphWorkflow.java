package com.kinch.agent.workflow;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 真正的 StateGraph 工作流实现
 * 
 * 基于 Spring AI Alibaba Graph Core 的完整实现
 * 参考官方文档：https://java2ai.com/docs/graph/guide
 * 
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealStateGraphWorkflow {

    private final ChatModel chatModel;

    // ========== 状态定义 ==========

    /**
     * 需求分类结构
     */
    public static class RequirementClassification {
        private String type;        // "simple", "complex", "technical", "business"
        private String complexity;  // "low", "medium", "high"
        private String category;    // 类别
        private String summary;     // 摘要

        public RequirementClassification() {}

        public RequirementClassification(String type, String complexity, String category, String summary) {
            this.type = type;
            this.complexity = complexity;
            this.category = category;
            this.summary = summary;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getComplexity() { return complexity; }
        public void setComplexity(String complexity) { this.complexity = complexity; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        @Override
        public String toString() {
            return String.format("RequirementClassification{type='%s', complexity='%s', category='%s', summary='%s'}", 
                    type, complexity, category, summary);
        }
    }

    /**
     * 配置状态键策略
     * 
     * 状态是图中所有节点的共享记忆
     */
    public static KeyStrategyFactory createKeyStrategyFactory() {
        return () -> {
            HashMap<String, KeyStrategy> strategies = new HashMap<>();
            // 输入数据
            strategies.put("requirement", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            // 分析结果
            strategies.put("classification", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("analysis", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("design", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("code", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("test_result", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            // 流程控制
            strategies.put("messages", new com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy());
            strategies.put("next_node", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("status", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("review_data", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            return strategies;
        };
    }

    // ========== 节点实现 ==========

    /**
     * 节点1：读取需求
     */
    public static class ReadRequirementNode implements NodeAction {
        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String requirement = state.value("requirement")
                    .map(v -> (String) v)
                    .orElse("");
            
            log.info("📋 读取需求: {}", requirement);
            
            List<String> messages = new ArrayList<>();
            messages.add("开始处理需求: " + requirement);
            
            return Map.of("messages", messages);
        }
    }

    /**
     * 节点2：分类需求
     */
    public static class ClassifyRequirementNode implements NodeAction {
        private final ChatClient chatClient;

        public ClassifyRequirementNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String requirement = state.value("requirement")
                    .map(v -> (String) v)
                    .orElseThrow(() -> new IllegalStateException("需求不能为空"));

            // 按需格式化提示
            String classificationPrompt = String.format("""
                    分析以下需求并分类：
                    
                    需求: %s
                    
                    请判断：
                    1. 类型（type）: simple/complex/technical/business
                    2. 复杂度（complexity）: low/medium/high
                    3. 类别（category）: web/backend/database/mobile/other
                    4. 摘要（summary）: 一句话概括
                    
                    返回JSON格式: {"type": "...", "complexity": "...", "category": "...", "summary": "..."}
                    """, requirement);

            String response = chatClient.prompt()
                    .user(classificationPrompt)
                    .call()
                    .content();

            // 解析分类结果
            RequirementClassification classification = parseClassification(response);
            
            log.info("🔍 需求分类: {}", classification);

            // 根据分类决定下一步
            String nextNode;
            if ("high".equals(classification.getComplexity()) || 
                "complex".equals(classification.getType())) {
                nextNode = "human_review";  // 复杂需求需要人工审核
            } else if ("simple".equals(classification.getType())) {
                nextNode = "quick_analysis";  // 简单需求快速分析
            } else {
                nextNode = "detailed_analysis";  // 常规需求详细分析
            }

            return Map.of(
                    "classification", classification,
                    "next_node", nextNode
            );
        }

        private RequirementClassification parseClassification(String jsonResponse) {
            RequirementClassification classification = new RequirementClassification();

            Pattern typePattern = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
            Pattern complexityPattern = Pattern.compile("\"complexity\"\\s*:\\s*\"([^\"]+)\"");
            Pattern categoryPattern = Pattern.compile("\"category\"\\s*:\\s*\"([^\"]+)\"");
            Pattern summaryPattern = Pattern.compile("\"summary\"\\s*:\\s*\"([^\"]+)\"");

            Matcher matcher = typePattern.matcher(jsonResponse);
            if (matcher.find()) classification.setType(matcher.group(1));

            matcher = complexityPattern.matcher(jsonResponse);
            if (matcher.find()) classification.setComplexity(matcher.group(1));

            matcher = categoryPattern.matcher(jsonResponse);
            if (matcher.find()) classification.setCategory(matcher.group(1));

            matcher = summaryPattern.matcher(jsonResponse);
            if (matcher.find()) classification.setSummary(matcher.group(1));

            // 默认值
            if (classification.getType() == null) classification.setType("simple");
            if (classification.getComplexity() == null) classification.setComplexity("medium");
            if (classification.getCategory() == null) classification.setCategory("general");
            if (classification.getSummary() == null) classification.setSummary("需求处理");

            return classification;
        }
    }

    /**
     * 节点3：快速分析（简单需求）
     */
    public static class QuickAnalysisNode implements NodeAction {
        private final ChatClient chatClient;

        public QuickAnalysisNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String requirement = state.value("requirement")
                    .map(v -> (String) v)
                    .orElse("");

            log.info("⚡ 快速分析简单需求");

            String analysisPrompt = String.format("""
                    快速分析以下简单需求：
                    
                    需求: %s
                    
                    请简要列出：
                    1. 核心功能点（1-2点）
                    2. 关键技术
                    3. 预计工作量
                    """, requirement);

            String analysis = chatClient.prompt()
                    .user(analysisPrompt)
                    .call()
                    .content();

            return Map.of(
                    "analysis", analysis,
                    "next_node", "generate_solution"
            );
        }
    }

    /**
     * 节点4：详细分析（常规需求）
     */
    public static class DetailedAnalysisNode implements NodeAction {
        private final ChatClient chatClient;

        public DetailedAnalysisNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String requirement = state.value("requirement")
                    .map(v -> (String) v)
                    .orElse("");

            log.info("🔬 详细分析需求");

            String analysisPrompt = String.format("""
                    详细分析以下需求：
                    
                    需求: %s
                    
                    请提供：
                    1. 核心功能分解（3-5点）
                    2. 技术要求和选型
                    3. 数据模型设计
                    4. 接口设计
                    5. 预期难点和风险
                    """, requirement);

            String analysis = chatClient.prompt()
                    .user(analysisPrompt)
                    .call()
                    .content();

            return Map.of(
                    "analysis", analysis,
                    "next_node", "generate_solution"
            );
        }
    }

    /**
     * 节点5：生成方案
     */
    public static class GenerateSolutionNode implements NodeAction {
        private final ChatClient chatClient;

        public GenerateSolutionNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String analysis = state.value("analysis")
                    .map(v -> (String) v)
                    .orElse("");

            log.info("🎨 生成技术方案");

            String designPrompt = String.format("""
                    基于以下需求分析，设计技术方案：
                    
                    分析结果: %s
                    
                    请提供：
                    1. 系统架构设计
                    2. 技术栈选择及理由
                    3. 核心模块设计
                    4. 数据库设计
                    5. 实施步骤
                    """, analysis);

            String design = chatClient.prompt()
                    .user(designPrompt)
                    .call()
                    .content();

            return Map.of(
                    "design", design,
                    "next_node", "generate_code"
            );
        }
    }

    /**
     * 节点6：生成代码
     */
    public static class GenerateCodeNode implements NodeAction {
        private final ChatClient chatClient;

        public GenerateCodeNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String design = state.value("design")
                    .map(v -> (String) v)
                    .orElse("");

            log.info("💻 生成代码");

            String codePrompt = String.format("""
                    根据以下技术方案，生成核心代码：
                    
                    方案: %s
                    
                    请生成：
                    1. 核心实体类（Entity）
                    2. 数据访问层（Repository）
                    3. 业务逻辑层（Service）
                    4. 控制器（Controller）
                    5. 必要的配置类
                    
                    使用 Spring Boot + JPA，代码要规范、有注释。
                    """, design);

            String code = chatClient.prompt()
                    .user(codePrompt)
                    .call()
                    .content();

            return Map.of(
                    "code", code,
                    "next_node", "generate_tests"
            );
        }
    }

    /**
     * 节点7：生成测试
     */
    public static class GenerateTestsNode implements NodeAction {
        private final ChatClient chatClient;

        public GenerateTestsNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String code = state.value("code")
                    .map(v -> (String) v)
                    .orElse("");

            log.info("✅ 生成测试用例");

            String testPrompt = String.format("""
                    为以下代码生成测试用例：
                    
                    代码: %s
                    
                    请生成：
                    1. 单元测试（JUnit 5）
                    2. 集成测试
                    3. 边界条件测试
                    4. 异常场景测试
                    5. Mock数据准备
                    
                    测试代码要全面、清晰。
                    """, code);

            String testResult = chatClient.prompt()
                    .user(testPrompt)
                    .call()
                    .content();

            return Map.of(
                    "test_result", testResult,
                    "status", "completed"
            );
        }
    }

    /**
     * 节点8：人工审核
     */
    public static class HumanReviewNode implements NodeAction {
        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            RequirementClassification classification = state.value("classification")
                    .map(v -> (RequirementClassification) v)
                    .orElse(new RequirementClassification());

            Map<String, Object> reviewData = Map.of(
                    "requirement", state.value("requirement").map(v -> (String) v).orElse(""),
                    "classification", classification.toString(),
                    "complexity", classification.getComplexity(),
                    "type", classification.getType(),
                    "action", "此需求较复杂，请人工审核确认是否继续"
            );

            log.info("👨‍💼 等待人工审核: {}", reviewData);

            return Map.of(
                    "review_data", reviewData,
                    "status", "waiting_for_review",
                    "next_node", "detailed_analysis"  // 审核通过后进入详细分析
            );
        }
    }

    // ========== Graph 构建 ==========

    /**
     * 创建开发工作流 StateGraph
     */
    public CompiledGraph createDevelopmentWorkflow() throws GraphStateException {
        log.info("🏗️ 创建开发工作流 StateGraph");

        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);

        // 创建节点
        var readRequirement = node_async(new ReadRequirementNode());
        var classifyRequirement = node_async(new ClassifyRequirementNode(chatClientBuilder));
        var quickAnalysis = node_async(new QuickAnalysisNode(chatClientBuilder));
        var detailedAnalysis = node_async(new DetailedAnalysisNode(chatClientBuilder));
        var generateSolution = node_async(new GenerateSolutionNode(chatClientBuilder));
        var generateCode = node_async(new GenerateCodeNode(chatClientBuilder));
        var generateTests = node_async(new GenerateTestsNode(chatClientBuilder));
        var humanReview = node_async(new HumanReviewNode());

        // 创建 StateGraph
        StateGraph workflow = new StateGraph(createKeyStrategyFactory())
                .addNode("read_requirement", readRequirement)
                .addNode("classify_requirement", classifyRequirement)
                .addNode("quick_analysis", quickAnalysis)
                .addNode("detailed_analysis", detailedAnalysis)
                .addNode("generate_solution", generateSolution)
                .addNode("generate_code", generateCode)
                .addNode("generate_tests", generateTests)
                .addNode("human_review", humanReview);

        // 添加边：固定流程
        workflow.addEdge(START, "read_requirement");
        workflow.addEdge("read_requirement", "classify_requirement");
        workflow.addEdge("generate_tests", END);

        // 添加条件边：根据分类结果路由
        workflow.addConditionalEdges("classify_requirement",
                edge_async(state -> {
                    return (String) state.value("next_node").orElse("detailed_analysis");
                }),
                Map.of(
                        "quick_analysis", "quick_analysis",
                        "detailed_analysis", "detailed_analysis",
                        "human_review", "human_review"
                ));

        // 分析后都进入方案生成
        workflow.addConditionalEdges("quick_analysis",
                edge_async(state -> {
                    return (String) state.value("next_node").orElse("generate_solution");
                }),
                Map.of("generate_solution", "generate_solution"));

        workflow.addConditionalEdges("detailed_analysis",
                edge_async(state -> {
                    return (String) state.value("next_node").orElse("generate_solution");
                }),
                Map.of("generate_solution", "generate_solution"));

        // 人工审核后进入详细分析
        workflow.addConditionalEdges("human_review",
                edge_async(state -> {
                    return (String) state.value("next_node").orElse("detailed_analysis");
                }),
                Map.of("detailed_analysis", "detailed_analysis"));

        // 方案->代码->测试 固定流程
        workflow.addConditionalEdges("generate_solution",
                edge_async(state -> {
                    return (String) state.value("next_node").orElse("generate_code");
                }),
                Map.of("generate_code", "generate_code"));

        workflow.addConditionalEdges("generate_code",
                edge_async(state -> {
                    return (String) state.value("next_node").orElse("generate_tests");
                }),
                Map.of("generate_tests", "generate_tests"));

        // 配置持久化和人工介入
        var memory = new MemorySaver();
        var compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(memory)
                        .build())
                .interruptBefore("human_review")  // 在人工审核前暂停
                .build();

        log.info("✅ StateGraph 编译完成");
        return workflow.compile(compileConfig);
    }

    /**
     * 执行工作流（兼容旧接口）
     */
    public Map<String, Object> executeWorkflow(Object graph, Map<String, Object> inputs) {
        log.info("执行工作流 - 输入: {}", inputs);
        
        if (!(graph instanceof CompiledGraph)) {
            log.warn("不是CompiledGraph，返回错误");
            return Map.of(
                "workflow_name", "development_workflow",
                "status", "failed",
                "error", "无效的图对象"
            );
        }

        try {
            CompiledGraph compiledGraph = (CompiledGraph) graph;
            
            // 创建配置
            var config = RunnableConfig.builder()
                    .threadId(UUID.randomUUID().toString())
                    .build();

            // 执行工作流
            var stream = compiledGraph.stream(inputs, config);
            stream.doOnNext(output -> log.info("节点输出: {}", output))
                  .doOnError(error -> log.error("执行错误: {}", error.getMessage()))
                  .blockLast();

            // 获取最终状态
            var finalState = compiledGraph.getState(config);
            Map<String, Object> stateData = finalState.state().data();

            return Map.of(
                "workflow_name", "development_workflow",
                "status", "success",
                "state", stateData
            );

        } catch (Exception e) {
            log.error("工作流执行失败", e);
            return Map.of(
                "workflow_name", "development_workflow",
                "status", "failed",
                "error", e.getMessage()
            );
        }
    }

    // ========== 条件分支工作流节点 ==========

    /**
     * 节点：内容分类
     */
    public static class ClassifyContentNode implements NodeAction {
        private final ChatClient chatClient;

        public ClassifyContentNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String input = state.value("input")
                    .map(v -> (String) v)
                    .orElseThrow(() -> new IllegalStateException("输入不能为空"));

            log.info("🔍 分类内容: {}", input);

            String classifyPrompt = String.format("""
                    请判断以下内容的类型（只回答类型，不要解释）：
                    
                    内容: %s
                    
                    请从以下选项中选择一个：
                    - technical（技术问题）
                    - business（业务需求）
                    - general（一般问题）
                    
                    只回答类型名称即可。
                    """, input);

            String response = chatClient.prompt()
                    .user(classifyPrompt)
                    .call()
                    .content()
                    .toLowerCase();

            // 确定类型
            String category;
            if (response.contains("technical") || response.contains("技术")) {
                category = "technical";
            } else if (response.contains("business") || response.contains("业务")) {
                category = "business";
            } else {
                category = "general";
            }

            log.info("✅ 分类结果: {}", category);

            return Map.of(
                    "category", category,
                    "next_node", category  // 直接用类型作为下一个节点
            );
        }
    }

    /**
     * 节点：处理技术问题
     */
    public static class HandleTechnicalNode implements NodeAction {
        private final ChatClient chatClient;

        public HandleTechnicalNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String input = state.value("input")
                    .map(v -> (String) v)
                    .orElse("");

            log.info("💻 处理技术问题");

            String technicalPrompt = String.format("""
                    作为技术专家，请解答以下技术问题：
                    
                    问题: %s
                    
                    请提供：
                    1. 问题分析
                    2. 技术原理
                    3. 解决方案（包含代码示例）
                    4. 最佳实践建议
                    5. 相关资源推荐
                    """, input);

            String result = chatClient.prompt()
                    .user(technicalPrompt)
                    .call()
                    .content();

            return Map.of(
                    "result", result,
                    "handler", "技术专家",
                    "status", "completed"
            );
        }
    }

    /**
     * 节点：处理业务需求
     */
    public static class HandleBusinessNode implements NodeAction {
        private final ChatClient chatClient;

        public HandleBusinessNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String input = state.value("input")
                    .map(v -> (String) v)
                    .orElse("");

            log.info("📊 处理业务需求");

            String businessPrompt = String.format("""
                    作为业务分析师，请分析以下业务需求：
                    
                    需求: %s
                    
                    请提供：
                    1. 需求理解和澄清
                    2. 业务价值分析
                    3. 用户场景和痛点
                    4. 实现建议
                    5. 潜在风险评估
                    6. 成功指标建议
                    """, input);

            String result = chatClient.prompt()
                    .user(businessPrompt)
                    .call()
                    .content();

            return Map.of(
                    "result", result,
                    "handler", "业务分析师",
                    "status", "completed"
            );
        }
    }

    /**
     * 节点：处理一般问题
     */
    public static class HandleGeneralNode implements NodeAction {
        private final ChatClient chatClient;

        public HandleGeneralNode(ChatClient.Builder chatClientBuilder) {
            this.chatClient = chatClientBuilder.build();
        }

        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String input = state.value("input")
                    .map(v -> (String) v)
                    .orElse("");

            log.info("💬 处理一般问题");

            String generalPrompt = String.format("""
                    请回答以下问题：
                    
                    问题: %s
                    
                    请提供清晰、准确、有帮助的回答。
                    """, input);

            String result = chatClient.prompt()
                    .user(generalPrompt)
                    .call()
                    .content();

            return Map.of(
                    "result", result,
                    "handler", "通用助手",
                    "status", "completed"
            );
        }
    }

    // ========== 条件分支工作流构建 ==========

    /**
     * 创建条件分支工作流
     * 
     * 根据输入类型自动选择处理路径：
     * - technical → 技术专家处理
     * - business → 业务分析师处理
     * - general → 通用助手处理
     */
    public CompiledGraph createConditionalWorkflow() throws GraphStateException {
        log.info("🏗️ 创建条件分支工作流 StateGraph");

        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);

        // 创建节点
        var classifyContent = node_async(new ClassifyContentNode(chatClientBuilder));
        var handleTechnical = node_async(new HandleTechnicalNode(chatClientBuilder));
        var handleBusiness = node_async(new HandleBusinessNode(chatClientBuilder));
        var handleGeneral = node_async(new HandleGeneralNode(chatClientBuilder));

        // 创建 StateGraph
        StateGraph workflow = new StateGraph(createConditionalKeyStrategyFactory())
                .addNode("classify", classifyContent)
                .addNode("technical", handleTechnical)
                .addNode("business", handleBusiness)
                .addNode("general", handleGeneral);

        // 添加边：START → 分类
        workflow.addEdge(START, "classify");

        // 添加条件边：根据分类结果路由到不同处理节点
        workflow.addConditionalEdges("classify",
                edge_async(state -> {
                    String nextNode = (String) state.value("next_node").orElse("general");
                    log.info("🔀 路由到: {}", nextNode);
                    return nextNode;
                }),
                Map.of(
                        "technical", "technical",
                        "business", "business",
                        "general", "general"
                ));

        // 所有处理节点都到 END
        workflow.addEdge("technical", END);
        workflow.addEdge("business", END);
        workflow.addEdge("general", END);

        // 配置（不需要持久化，简单流程）
        var compileConfig = CompileConfig.builder().build();

        log.info("✅ 条件分支工作流编译完成");
        return workflow.compile(compileConfig);
    }

    /**
     * 条件工作流的状态键策略
     */
    private static KeyStrategyFactory createConditionalKeyStrategyFactory() {
        return () -> {
            HashMap<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("input", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("category", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("result", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("handler", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("next_node", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            strategies.put("status", new com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy());
            return strategies;
        };
    }

    /**
     * 执行条件工作流
     */
    public Map<String, Object> executeConditionalWorkflow(String input) {
        log.info("执行条件分支工作流 - 输入: {}", input);
        
        try {
            CompiledGraph graph = createConditionalWorkflow();
            
            Map<String, Object> inputs = Map.of("input", input);
            
            var config = RunnableConfig.builder()
                    .threadId(UUID.randomUUID().toString())
                    .build();

            // 执行工作流
            var stream = graph.stream(inputs, config);
            stream.doOnNext(output -> log.info("节点输出: {}", output))
                  .doOnError(error -> log.error("执行错误: {}", error.getMessage()))
                  .blockLast();

            // 获取最终状态
            var finalState = graph.getState(config);
            Map<String, Object> stateData = finalState.state().data();

            return Map.of(
                "workflow_name", "conditional_workflow",
                "status", "success",
                "state", stateData
            );

        } catch (Exception e) {
            log.error("条件工作流执行失败", e);
            return Map.of(
                "workflow_name", "conditional_workflow",
                "status", "failed",
                "error", e.getMessage()
            );
        }
    }
}
