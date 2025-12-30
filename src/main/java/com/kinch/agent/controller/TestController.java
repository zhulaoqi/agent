package com.kinch.agent.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.kinch.agent.config.AgentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 测试Controller - 用于快速测试ReactAgent功能
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestController {

    private final AgentConfig.AgentFactory agentFactory;

    /**
     * 测试对话接口
     */
    @GetMapping("/chat")
    public Map<String, Object> testChat(@RequestParam String message) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("收到测试请求: {}", message);
            
            // 构建Agent
            ReactAgent agent = agentFactory.createAgent("test_agent");

            // 配置
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(UUID.randomUUID().toString())
                    .build();

            // 调用
            AssistantMessage response = agent.call(message, config);
            
            result.put("success", true);
            result.put("message", response.getText());
            result.put("timestamp", System.currentTimeMillis());
            
            log.info("响应成功: {}", response.getText());

        } catch (Exception e) {
            log.error("调用失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("agent", "ReactAgent");
        health.put("tools", "weather, naming, script");
        return health;
    }

    /**
     * 测试工具调用
     */
    @GetMapping("/tool-test")
    public Map<String, Object> toolTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试天气查询工具
            String weatherQuery = "北京今天天气怎么样？";
            
            ReactAgent agent = agentFactory.createAgent("tool_test_agent");

            RunnableConfig config = RunnableConfig.builder()
                    .threadId(UUID.randomUUID().toString())
                    .build();

            AssistantMessage response = agent.call(weatherQuery, config);
            
            result.put("success", true);
            result.put("query", weatherQuery);
            result.put("response", response.getText());
            
        } catch (Exception e) {
            log.error("工具测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}

