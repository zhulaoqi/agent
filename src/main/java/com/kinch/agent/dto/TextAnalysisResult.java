package com.kinch.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.util.List;

/**
 * 文本分析结果 - 结构化输出
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
public class TextAnalysisResult {

    @JsonPropertyDescription("情感倾向：positive、negative、neutral")
    private String sentiment;

    @JsonPropertyDescription("情感得分：-1.0到1.0")
    private Double sentimentScore;

    @JsonPropertyDescription("文本摘要")
    private String summary;

    @JsonPropertyDescription("关键词列表")
    private List<String> keywords;

    @JsonPropertyDescription("主要主题")
    private List<String> topics;

    @JsonPropertyDescription("置信度：0-100")
    private Integer confidence;

    @JsonPropertyDescription("分析建议")
    private String suggestion;
}



