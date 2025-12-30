package com.kinch.agent.dto;

import lombok.Data;

/**
 * PPT生成请求
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
public class PptRequest {
    
    /**
     * PPT主题
     */
    private String theme;
    
    /**
     * 内容描述
     */
    private String content;
    
    /**
     * 模板样式（可选）
     */
    private String template;
}

