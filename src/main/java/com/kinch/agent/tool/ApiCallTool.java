package com.kinch.agent.tool;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

/**
 * API调用工具
 * 支持HTTP GET/POST请求
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallTool implements Function<String, String> {

    private final RestTemplate restTemplate;
    private final Gson gson;

    @Override
    public String apply(String input) {
        log.info("API调用工具 - 输入: {}", input);

        try {
            // 解析输入（格式：method|url|body）
            String[] parts = input.split("\\|", 3);
            if (parts.length < 2) {
                return "❌ 参数格式错误，应为：方法|URL[|请求体]";
            }

            String method = parts[0].trim().toUpperCase();
            String url = parts[1].trim();
            String body = parts.length > 2 ? parts[2].trim() : null;

            // 安全检查：只允许HTTP/HTTPS
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return "❌ 安全限制：只允许HTTP/HTTPS协议";
            }

            // 执行请求
            String response;
            if ("GET".equals(method)) {
                response = restTemplate.getForObject(url, String.class);
            } else if ("POST".equals(method)) {
                response = restTemplate.postForObject(url, body, String.class);
            } else {
                return "❌ 不支持的HTTP方法：" + method;
            }

            // 格式化响应
            return "✅ API调用成功\n\n" +
                   "请求: " + method + " " + url + "\n" +
                   "响应: \n" + formatJson(response);

        } catch (Exception e) {
            log.error("API调用失败", e);
            return "❌ API调用失败: " + e.getMessage();
        }
    }

    private String formatJson(String json) {
        try {
            Object obj = gson.fromJson(json, Object.class);
            return gson.toJson(obj);
        } catch (Exception e) {
            return json;
        }
    }
}

