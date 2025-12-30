package com.kinch.agent.tool;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 脚本生成工具 - 为运维人员生成Shell、Python等脚本
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScriptGenerationTool implements Function<String, String> {

    private final ChatClient.Builder chatClientBuilder;
    private final Gson gson = new Gson();

    @Override
    public String apply(String input) {

        log.info("生成脚本 - 输入: {}", input);

        try {
            // 解析输入
            String[] parts = input.split("\\|", 2);
            if (parts.length < 2) {
                return "{\"error\": \"输入格式错误，应为：类型|描述\"}";
            }

            String scriptType = parts[0].trim();
            String requirement = parts[1].trim();

            // 生成脚本
            String scriptContent = generateScript(requirement, scriptType);

            // 保存脚本文件
            String scriptPath = saveScript(scriptContent, scriptType);

            Map<String, Object> result = new HashMap<>();
            result.put("scriptType", scriptType);
            result.put("requirement", requirement);
            result.put("scriptPath", scriptPath);
            result.put("scriptContent", scriptContent);
            result.put("status", "success");

            return gson.toJson(result);

        } catch (Exception e) {
            log.error("脚本生成失败", e);
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    private String generateScript(String requirement, String scriptType) {
        String prompt = String.format(
                "请根据以下需求生成一个%s脚本：\n%s\n\n" +
                        "要求：\n" +
                        "1. 代码要规范、注释清晰\n" +
                        "2. 包含错误处理\n" +
                        "3. 遵循最佳实践\n" +
                        "4. 只返回脚本代码，不要markdown代码块标记\n" +
                        "5. 如果是shell脚本，第一行添加#!/bin/bash",
                scriptType, requirement
        );

        ChatClient chatClient = chatClientBuilder.build();
        ChatResponse response = chatClient.prompt().user(prompt).call().chatResponse();
        String scriptContent = response.getResult().getOutput().getText();

        // 清理代码块标记
        scriptContent = scriptContent.replaceAll("```[a-z]*\\n?", "").trim();

        return scriptContent;
    }

    private String saveScript(String scriptContent, String scriptType) throws Exception {
        // 创建保存目录
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Path dir = Paths.get("data", "scripts", dateStr);
        Files.createDirectories(dir);

        // 生成文件名
        String extension = getScriptExtension(scriptType);
        String fileName = String.format("script_%d.%s", System.currentTimeMillis(), extension);
        Path filePath = dir.resolve(fileName);

        // 保存文件
        try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
            out.write(scriptContent.getBytes());
        }

        return filePath.toString();
    }

    private String getScriptExtension(String scriptType) {
        switch (scriptType.toLowerCase()) {
            case "shell":
            case "bash":
                return "sh";
            case "python":
                return "py";
            case "powershell":
                return "ps1";
            case "javascript":
                return "js";
            default:
                return "txt";
        }
    }
}

