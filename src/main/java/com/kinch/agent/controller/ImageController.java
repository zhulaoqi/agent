package com.kinch.agent.controller;

import com.kinch.agent.service.ImageGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片生成控制器
 * 
 * @author kinch
 * @date 2025-12-30
 */
@Slf4j
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageGenerationService imageGenerationService;

    /**
     * 生成图片
     */
    @PostMapping("/generate")
    public Map<String, Object> generateImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        log.info("生成图片 - 提示词: {}", prompt);

        Map<String, Object> response = new HashMap<>();
        try {
            String imageUrl = imageGenerationService.generateImage(prompt);
            
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("prompt", prompt);
            
        } catch (Exception e) {
            log.error("图片生成失败", e);
            response.put("success", false);
            response.put("message", "生成失败: " + e.getMessage());
        }
        
        return response;
    }
}


