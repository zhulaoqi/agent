package com.kinch.agent.service;

import com.kinch.agent.tool.WeatherTool;
import com.kinch.agent.tool.NamingTool;
import com.kinch.agent.tool.ScriptGenerationTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 流式响应服务
 * 实现SSE (Server-Sent Events) 流式输出
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

    private final ChatClient.Builder chatClientBuilder;
    private final WeatherTool weatherTool;
    private final NamingTool namingTool;
    private final ScriptGenerationTool scriptGenerationTool;

    /**
     * 流式聊天 - 带工具支持
     */
    public Flux<String> streamChat(String message) {
        log.info("流式聊天 - 消息: {}", message);

        // 配置工具回调
        ToolCallback weatherCallback = FunctionToolCallback.builder("get_weather", weatherTool)
                .description("查询指定城市的天气信息")
                .inputType(String.class)
                .build();

        ToolCallback namingCallback = FunctionToolCallback.builder("generate_name", namingTool)
                .description("根据中文描述生成变量命名")
                .inputType(String.class)
                .build();

        ToolCallback scriptCallback = FunctionToolCallback.builder("generate_script", scriptGenerationTool)
                .description("生成运维脚本")
                .inputType(String.class)
                .build();

        // 创建带工具的ChatClient
        ChatClient chatClient = chatClientBuilder
                .defaultTools(weatherCallback, namingCallback, scriptCallback)
                .build();

        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    /**
     * 流式生成代码
     */
    public Flux<String> streamCodeGeneration(String requirement, String language) {
        log.info("流式生成代码 - 语言: {}, 需求: {}", language, requirement);

        String prompt = String.format(
            "请用%s语言实现以下功能：%s\n\n请直接输出代码，包含注释。", 
            language, requirement
        );

        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    /**
     * 流式文本分析
     */
    public Flux<String> streamAnalysis(String text) {
        log.info("流式文本分析 - 文本长度: {}", text.length());

        String prompt = String.format(
            "请详细分析以下文本，包括：\n" +
            "1. 主题和要点\n" +
            "2. 情感倾向\n" +
            "3. 关键信息提取\n" +
            "4. 总结\n\n" +
            "文本内容：\n%s", 
            text
        );

        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    /**
     * 流式翻译
     */
    public Flux<String> streamTranslation(String text, String targetLanguage) {
        log.info("流式翻译 - 目标语言: {}", targetLanguage);

        String prompt = String.format(
            "请将以下内容翻译成%s，保持原文的风格和语气：\n\n%s", 
            targetLanguage, text
        );

        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}


