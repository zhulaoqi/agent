package com.kinch.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.*;
import org.springframework.stereotype.Service;

/**
 * 图片生成服务
 * 
 * @author kinch
 * @date 2025-12-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final ImageModel imageModel;

    /**
     * 生成图片
     */
    public String generateImage(String prompt) {
        log.info("生成图片 - 提示词: {}", prompt);

        try {
            // 创建图片生成选项
            ImageOptions options = ImageOptionsBuilder.builder()
                    .model("wanx-v1")
                    .N(1)
                    .width(1024)
                    .height(1024)
                    .build();

            // 创建提示
            ImagePrompt imagePrompt = new ImagePrompt(prompt, options);

            // 调用模型生成图片
            ImageResponse response = imageModel.call(imagePrompt);

            // 获取图片URL
            String imageUrl = response.getResult().getOutput().getUrl();
            
            log.info("图片生成成功 - URL: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("图片生成失败", e);
            throw new RuntimeException("图片生成失败: " + e.getMessage(), e);
        }
    }
}

