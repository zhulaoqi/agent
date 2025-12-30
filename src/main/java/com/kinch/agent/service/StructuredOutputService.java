package com.kinch.agent.service;

import com.kinch.agent.dto.StructuredWeatherResponse;
import com.kinch.agent.dto.TextAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * 结构化输出服务
 * 展示Spring AI的Structured Output能力
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StructuredOutputService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 结构化天气分析
     */
    public StructuredWeatherResponse analyzeWeather(String city, String weatherData) {
        log.info("结构化天气分析 - 城市: {}", city);

        BeanOutputConverter<StructuredWeatherResponse> converter = 
            new BeanOutputConverter<>(StructuredWeatherResponse.class);

        String prompt = String.format("""
            请分析以下天气数据并给出建议：
            
            城市：%s
            数据：%s
            
            %s
            """, city, weatherData, converter.getFormat());

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return converter.convert(response);
    }

    /**
     * 结构化文本分析
     */
    public TextAnalysisResult analyzeText(String text) {
        log.info("结构化文本分析 - 文本长度: {}", text.length());

        BeanOutputConverter<TextAnalysisResult> converter = 
            new BeanOutputConverter<>(TextAnalysisResult.class);

        String prompt = String.format("""
            请分析以下文本的情感、主题和关键信息：
            
            文本：%s
            
            %s
            """, text, converter.getFormat());

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return converter.convert(response);
    }

    /**
     * 结构化代码审查
     */
    public CodeReviewResult reviewCode(String code, String language) {
        log.info("结构化代码审查 - 语言: {}", language);

        BeanOutputConverter<CodeReviewResult> converter = 
            new BeanOutputConverter<>(CodeReviewResult.class);

        String prompt = String.format("""
            请审查以下%s代码，给出结构化的审查意见：
            
            ```%s
            %s
            ```
            
            %s
            """, language, language, code, converter.getFormat());

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return converter.convert(response);
    }

    /**
     * 代码审查结果
     */
    public static class CodeReviewResult {
        public Integer qualityScore;        // 质量评分
        public String overallComment;       // 总体评价
        public java.util.List<Issue> issues; // 问题列表
        public java.util.List<String> strengths;  // 优点
        public java.util.List<String> suggestions; // 改进建议
        
        public static class Issue {
            public String severity;  // 严重程度
            public String line;      // 行号
            public String description; // 问题描述
            public String suggestion;  // 修复建议
        }
    }
}



