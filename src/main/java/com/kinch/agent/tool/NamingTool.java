package com.kinch.agent.tool;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 命名工具 - 简单版：翻译 + 规范生成
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NamingTool implements Function<String, String> {

    private final ChatClient.Builder chatClientBuilder;
    private final Gson gson = new Gson();

    @Override
    public String apply(String chineseInput) {

        log.info("命名工具 - 输入: {}", chineseInput);

        try {
            // 1. 翻译成英文
            ChatClient chatClient = chatClientBuilder.build();
            String prompt = "将以下中文翻译成简洁的英文（只返回英文单词或短语，多个单词用空格分隔）：" + chineseInput;
            
            String english = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content()
                    .trim()
                    .replaceAll("[^a-zA-Z\\s]", "");
            
            log.info("翻译结果: {} -> {}", chineseInput, english);
            
            // 2. 按规范生成各种格式
            Map<String, String> result = new HashMap<>();
            result.put("original", chineseInput);
            result.put("english", english);
            
            String[] words = english.split("\\s+");
            result.put("PascalCase", toPascalCase(words));       // 类名
            result.put("camelCase", toCamelCase(words));         // 变量名/方法名
            result.put("UPPER_SNAKE", toUpperSnake(words));      // 常量名
            result.put("snake_case", toSnake(words));            // 表名/字段名
            result.put("kebab-case", toKebab(words));            // URL/文件名
            
            return gson.toJson(result);

        } catch (Exception e) {
            log.error("命名生成失败", e);
            return gson.toJson(Map.of("error", e.getMessage()));
        }
    }

    private String toPascalCase(String[] words) {
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private String toCamelCase(String[] words) {
        if (words.length == 0) return "";
        StringBuilder sb = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                sb.append(Character.toUpperCase(words[i].charAt(0)))
                  .append(words[i].substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private String toUpperSnake(String[] words) {
        return String.join("_", words).toUpperCase();
    }

    private String toSnake(String[] words) {
        return String.join("_", words).toLowerCase();
    }

    private String toKebab(String[] words) {
        return String.join("-", words).toLowerCase();
    }
}
