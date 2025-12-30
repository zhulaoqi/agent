package com.kinch.agent.controller;

import com.kinch.agent.tool.NamingTool;
import com.kinch.agent.tool.ScriptGenerationTool;
import com.kinch.agent.tool.WeatherTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具控制器
 * 处理各种工具调用请求
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/tool")
@RequiredArgsConstructor
public class ToolController {

    private final WeatherTool weatherTool;
    private final NamingTool namingTool;
    private final ScriptGenerationTool scriptGenerationTool;

    /**
     * 天气查询
     */
    @PostMapping("/weather")
    public Map<String, Object> queryWeather(@RequestBody Map<String, String> request) {
        String city = request.get("city");
        
        log.info("查询天气 - 城市: {}", city);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String result = weatherTool.apply(city);
            
            response.put("success", true);
            response.put("data", result);
            response.put("city", city);
            
        } catch (Exception e) {
            log.error("天气查询失败", e);
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 命名助手
     */
    @PostMapping("/naming")
    public Map<String, Object> generateNaming(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        
        log.info("生成命名 - 描述: {}", description);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String result = namingTool.apply(description);
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", result);
            
        } catch (Exception e) {
            log.error("命名生成失败", e);
            response.put("success", false);
            response.put("message", "生成失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 脚本生成
     */
    @PostMapping("/script")
    public Map<String, Object> generateScript(@RequestBody Map<String, String> request) {
        String requirements = request.get("requirements");
        String language = request.getOrDefault("language", "shell");
        
        log.info("生成脚本 - 语言: {}, 需求: {}", language, requirements);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String prompt = String.format("生成%s脚本：%s", language, requirements);
            String result = scriptGenerationTool.apply(prompt);
            
            response.put("success", true);
            response.put("data", result);
            response.put("message", result);
            response.put("script", result);
            
        } catch (Exception e) {
            log.error("脚本生成失败", e);
            response.put("success", false);
            response.put("message", "生成失败: " + e.getMessage());
        }
        
        return response;
    }
}

