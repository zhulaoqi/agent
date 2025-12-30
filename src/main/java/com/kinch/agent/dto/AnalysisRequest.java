package com.kinch.agent.dto;

import lombok.Data;

/**
 * 分析请求
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
public class AnalysisRequest {
    
    /**
     * 要分析的文本内容
     */
    private String text;
    
    /**
     * 分析类型：sentiment（情感）、summary（摘要）、keywords（关键词）
     */
    private String type;
}



