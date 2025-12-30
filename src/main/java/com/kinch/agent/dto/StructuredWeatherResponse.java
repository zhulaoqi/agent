package com.kinch.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.util.List;

/**
 * 结构化天气响应 - 用于演示Structured Output
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
public class StructuredWeatherResponse {

    @JsonPropertyDescription("城市名称")
    private String city;

    @JsonPropertyDescription("当前温度")
    private Integer temperature;

    @JsonPropertyDescription("天气状况：晴、多云、阴、雨等")
    private String condition;

    @JsonPropertyDescription("湿度百分比")
    private Integer humidity;

    @JsonPropertyDescription("空气质量指数")
    private Integer aqi;

    @JsonPropertyDescription("空气质量等级：优、良、轻度污染等")
    private String aqiLevel;

    @JsonPropertyDescription("今日穿衣建议")
    private String dressingAdvice;

    @JsonPropertyDescription("出行建议")
    private List<String> travelTips;

    @JsonPropertyDescription("健康提示")
    private String healthTip;
}

