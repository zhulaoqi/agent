package com.kinch.agent.controller;

import com.kinch.agent.service.StreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 流式响应控制器
 * 支持SSE流式输出
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    /**
     * 流式聊天
     * 客户端使用 EventSource 或 fetch 接收流式数据
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("流式聊天请求 - 消息: {}", message);

        return streamingService.streamChat(message)
                .map(chunk -> "data: " + chunk + "\n\n")
                .onErrorResume(e -> {
                    log.error("流式聊天错误", e);
                    return Flux.just("data: [ERROR] " + e.getMessage() + "\n\n");
                });
    }

    /**
     * 流式代码生成
     */
    @PostMapping(value = "/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamCode(@RequestBody Map<String, String> request) {
        String requirement = request.get("requirement");
        String language = request.getOrDefault("language", "Java");
        log.info("流式代码生成 - 语言: {}", language);

        return streamingService.streamCodeGeneration(requirement, language)
                .map(chunk -> "data: " + chunk + "\n\n")
                .onErrorResume(e -> {
                    log.error("流式代码生成错误", e);
                    return Flux.just("data: [ERROR] " + e.getMessage() + "\n\n");
                });
    }

    /**
     * 流式文本分析
     */
    @PostMapping(value = "/analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAnalysis(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        log.info("流式分析请求");

        return streamingService.streamAnalysis(text)
                .map(chunk -> "data: " + chunk + "\n\n")
                .onErrorResume(e -> {
                    log.error("流式分析错误", e);
                    return Flux.just("data: [ERROR] " + e.getMessage() + "\n\n");
                });
    }

    /**
     * 流式翻译
     */
    @PostMapping(value = "/translate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamTranslate(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String targetLanguage = request.getOrDefault("targetLanguage", "English");
        log.info("流式翻译 - 目标语言: {}", targetLanguage);

        return streamingService.streamTranslation(text, targetLanguage)
                .map(chunk -> "data: " + chunk + "\n\n")
                .onErrorResume(e -> {
                    log.error("流式翻译错误", e);
                    return Flux.just("data: [ERROR] " + e.getMessage() + "\n\n");
                });
    }
}


