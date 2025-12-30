package com.kinch.agent.controller;

import com.kinch.agent.dto.PptRequest;
import com.kinch.agent.service.PptGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * PPT生成控制器
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/ppt")
@RequiredArgsConstructor
public class PptController {

    private final PptGenerationService pptGenerationService;

    /**
     * 生成PPT
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePpt(@RequestBody PptRequest request) {
        log.info("收到PPT生成请求 - 主题: {}", request.getTheme());

        try {
            byte[] pptData = pptGenerationService.generatePpt(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                request.getTheme() + ".pptx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pptData);

        } catch (IOException e) {
            log.error("PPT生成失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


