package com.kinch.agent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.kinch.agent.hook.CustomAuditHook;
import com.kinch.agent.hook.CustomSecurityHook;
import com.kinch.agent.hook.CustomTokenLimitHook;
import com.kinch.agent.hook.MessageTrimmingHook;
import com.kinch.agent.interceptor.PerformanceInterceptor;
import com.kinch.agent.interceptor.SafetyInterceptor;
import com.kinch.agent.interceptor.ToolMonitorInterceptor;
import com.kinch.agent.tool.NamingTool;
import com.kinch.agent.tool.ScriptGenerationTool;
import com.kinch.agent.tool.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Agent配置类 - 配置Spring AI Alibaba组件
 *
 * @author kinch
 * @date 2025-12-29
 */
@Configuration
public class AgentConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${agent.max-iterations:10}")
    private Integer maxIterations;

    private final WeatherTool weatherTool;
    private final NamingTool namingTool;
    private final ScriptGenerationTool scriptGenerationTool;

    // 使用构造函数注入，并用@Lazy解决循环依赖
    public AgentConfig(
            @Lazy WeatherTool weatherTool,
            @Lazy NamingTool namingTool,
            @Lazy ScriptGenerationTool scriptGenerationTool,
            CustomAuditHook customAuditHook,
            CustomSecurityHook customSecurityHook,
            CustomTokenLimitHook customTokenLimitHook,
            MessageTrimmingHook messageTrimmingHook,
            PerformanceInterceptor performanceInterceptor,
            SafetyInterceptor safetyInterceptor,
            ToolMonitorInterceptor toolMonitorInterceptor) {
        this.weatherTool = weatherTool;
        this.namingTool = namingTool;
        this.scriptGenerationTool = scriptGenerationTool;
        this.customAuditHook = customAuditHook;
        this.customSecurityHook = customSecurityHook;
        this.customTokenLimitHook = customTokenLimitHook;
        this.messageTrimmingHook = messageTrimmingHook;
        this.performanceInterceptor = performanceInterceptor;
        this.safetyInterceptor = safetyInterceptor;
        this.toolMonitorInterceptor = toolMonitorInterceptor;
    }

    // Hooks和Interceptors - 使用框架的Hook机制
    private final CustomAuditHook customAuditHook;
    private final CustomSecurityHook customSecurityHook;
    private final CustomTokenLimitHook customTokenLimitHook;
    private final MessageTrimmingHook messageTrimmingHook;
    private final PerformanceInterceptor performanceInterceptor;
    private final SafetyInterceptor safetyInterceptor;
    private final ToolMonitorInterceptor toolMonitorInterceptor;

    /**
     * RestTemplate配置 - 用于HTTP请求
     */
    @Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        return new org.springframework.web.client.RestTemplate();
    }

    /**
     * Gson配置 - 用于JSON处理
     */
    @Bean
    public com.google.gson.Gson gson() {
        return new com.google.gson.Gson();
    }

    /**
     * DashScope API配置
     */
    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * ChatModel配置
     */
    @Bean
    public ChatModel chatModel(DashScopeApi dashScopeApi) {
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .model("qwen-plus")  // 使用新API，不带with前缀
                        .temperature(0.7)
                        .maxToken(2000)
                        .build())
                .build();
    }

    /**
     * DashScope ImageApi配置
     */
    @Bean
    public DashScopeImageApi dashScopeImageApi() {
        return DashScopeImageApi.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * ImageModel配置
     */
    @Bean
    public ImageModel imageModel(DashScopeImageApi dashScopeImageApi) {
        return DashScopeImageModel.builder()
                .dashScopeApi(dashScopeImageApi)
                .build();
    }

    /**
     * ChatClient Builder配置
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    /**
     * 天气查询工具回调
     */
    @Bean
    public ToolCallback weatherToolCallback() {
        return FunctionToolCallback.builder("getWeather", weatherTool)
                .description("查询指定城市的天气信息，包括温度、湿度、风速等")
                .inputType(String.class)
                .build();
    }

    /**
     * 命名助手工具回调
     */
    @Bean
    public ToolCallback namingToolCallback() {
        return FunctionToolCallback.builder("generateNaming", namingTool)
                .description("为技术人员生成变量名、类名、方法名等命名建议")
                .inputType(String.class)
                .build();
    }

    /**
     * 脚本生成工具回调
     */
    @Bean
    public ToolCallback scriptToolCallback() {
        return FunctionToolCallback.builder("generateScript", scriptGenerationTool)
                .description("为运维人员生成Shell、Python等运维脚本")
                .inputType(String.class)
                .build();
    }

    /**
     * ModelCallLimit Hook配置
     */
    @Bean
    public ModelCallLimitHook modelCallLimitHook() {
        return ModelCallLimitHook.builder()
                .runLimit(maxIterations)
                .exitBehavior(ModelCallLimitHook.ExitBehavior.ERROR)
                .build();
    }

    /**
     * React Agent配置工厂
     * 提供创建ReactAgent的方法，集成所有Hooks和Interceptors
     */
    @Bean
    public AgentFactory agentFactory(
            ChatModel chatModel,
            ToolCallback weatherToolCallback,
            ToolCallback namingToolCallback,
            ToolCallback scriptToolCallback,
            ModelCallLimitHook modelCallLimitHook,
            CustomAuditHook customAuditHook,
            CustomSecurityHook customSecurityHook,
            CustomTokenLimitHook customTokenLimitHook,
            MessageTrimmingHook messageTrimmingHook,
            PerformanceInterceptor performanceInterceptor,
            SafetyInterceptor safetyInterceptor,
            ToolMonitorInterceptor toolMonitorInterceptor) {

        return new AgentFactory(chatModel, weatherToolCallback, namingToolCallback,
                scriptToolCallback, modelCallLimitHook, customAuditHook, customSecurityHook,
                customTokenLimitHook, messageTrimmingHook, performanceInterceptor, 
                safetyInterceptor, toolMonitorInterceptor);
    }
    
    /**
     * AgentFactory内部类 - 用于创建ReactAgent实例
     * 集成完整的Hooks和Interceptors生态
     */
    public static class AgentFactory {
        private final ChatModel chatModel;
        private final ToolCallback weatherToolCallback;
        private final ToolCallback namingToolCallback;
        private final ToolCallback scriptToolCallback;
        private final ModelCallLimitHook modelCallLimitHook;
        private final CustomAuditHook customAuditHook;
        private final CustomSecurityHook customSecurityHook;
        private final CustomTokenLimitHook customTokenLimitHook;
        private final MessageTrimmingHook messageTrimmingHook;
        private final PerformanceInterceptor performanceInterceptor;
        private final SafetyInterceptor safetyInterceptor;
        private final ToolMonitorInterceptor toolMonitorInterceptor;
        
        public AgentFactory(ChatModel chatModel, ToolCallback weatherToolCallback,
                ToolCallback namingToolCallback, ToolCallback scriptToolCallback,
                ModelCallLimitHook modelCallLimitHook, CustomAuditHook customAuditHook,
                CustomSecurityHook customSecurityHook, CustomTokenLimitHook customTokenLimitHook,
                MessageTrimmingHook messageTrimmingHook, PerformanceInterceptor performanceInterceptor,
                SafetyInterceptor safetyInterceptor, ToolMonitorInterceptor toolMonitorInterceptor) {
            this.chatModel = chatModel;
            this.weatherToolCallback = weatherToolCallback;
            this.namingToolCallback = namingToolCallback;
            this.scriptToolCallback = scriptToolCallback;
            this.modelCallLimitHook = modelCallLimitHook;
            this.customAuditHook = customAuditHook;
            this.customSecurityHook = customSecurityHook;
            this.customTokenLimitHook = customTokenLimitHook;
            this.messageTrimmingHook = messageTrimmingHook;
            this.performanceInterceptor = performanceInterceptor;
            this.safetyInterceptor = safetyInterceptor;
            this.toolMonitorInterceptor = toolMonitorInterceptor;
        }
        
        /**
         * 创建新的ReactAgent实例 - 完整配置版
         * 集成所有Hooks和Interceptors
         */
        public ReactAgent create() {
            return createAgent("intelligent_agent");
        }
        
        /**
         * 创建指定名称的ReactAgent实例
         */
        public ReactAgent createAgent(String name) {
            return ReactAgent.builder()
                    .model(chatModel)
                    .name(name)
                    .tools(weatherToolCallback, namingToolCallback, scriptToolCallback)
                    // ===== Hooks配置 =====
                    .hooks(
                        modelCallLimitHook,      // 模型调用次数限制（框架内置）
                        customAuditHook,         // 审计日志记录（自定义）
                        customSecurityHook,      // 安全检查（自定义）
                        customTokenLimitHook,    // Token限流（自定义）
                        messageTrimmingHook      // 消息自动修剪（自定义）
                    )
                    // ===== Interceptors配置 =====
                    .interceptors(
                        performanceInterceptor,  // 性能监控
                        safetyInterceptor        // 安全检查
                    )
                    .interceptors(
                        toolMonitorInterceptor   // 工具调用监控
                    )
                    .systemPrompt("""
                            你是一个智能AI助手，基于Spring AI Alibaba框架构建。
                            
                            🛠️ 可用工具：
                            1. getWeather - 查询城市天气信息（支持高德地图API）
                            2. generateNaming - 生成编程命名建议（支持中文翻译）
                            3. generateScript - 生成运维脚本（Shell/Python/等）
                            
                            🔒 安全特性：
                            - 自动审计日志记录
                            - 敏感信息过滤
                            - SQL注入防护
                            - Token使用控制
                            
                            📊 智能优化：
                            - 上下文自动修剪
                            - 性能实时监控
                            - 工具调用追踪
                            
                            请根据用户需求，智能选择合适的工具，提供专业、安全的服务。
                            """)
                    .build();
        }
    }
}


