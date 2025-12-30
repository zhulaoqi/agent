package com.kinch.agent.controller;

import com.kinch.agent.entity.PromptTemplate;
import com.kinch.agent.service.PromptManagementServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词管理API
 * 
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@RestController
@RequestMapping("/api/prompt")
@RequiredArgsConstructor
public class PromptController {

    private final PromptManagementServiceV2 promptService;

    /**
     * 获取所有启用的模板
     */
    @GetMapping("/templates")
    public Map<String, Object> listTemplates() {
        List<PromptTemplate> templates = promptService.listActiveTemplates();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", templates);
        response.put("total", templates.size());
        
        return response;
    }

    /**
     * 根据名称获取模板
     */
    @GetMapping("/templates/{name}")
    public Map<String, Object> getTemplate(@PathVariable String name) {
        Map<String, Object> response = new HashMap<>();
        
        promptService.getTemplate(name).ifPresentOrElse(
            template -> {
                response.put("success", true);
                response.put("data", template);
            },
            () -> {
                response.put("success", false);
                response.put("message", "模板不存在: " + name);
            }
        );
        
        return response;
    }

    /**
     * 按分类查询模板
     */
    @GetMapping("/templates/category/{category}")
    public Map<String, Object> listByCategory(@PathVariable String category) {
        List<PromptTemplate> templates = promptService.listByCategory(category);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", templates);
        response.put("category", category);
        response.put("total", templates.size());
        
        return response;
    }

    /**
     * 搜索模板
     */
    @GetMapping("/templates/search")
    public Map<String, Object> search(@RequestParam String keyword) {
        List<PromptTemplate> templates = promptService.search(keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", templates);
        response.put("keyword", keyword);
        response.put("total", templates.size());
        
        return response;
    }

    /**
     * 获取热门模板
     */
    @GetMapping("/popular")
    public Map<String, Object> getPopular() {
        List<PromptTemplate> templates = promptService.getPopularTemplates();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", templates);
        response.put("total", templates.size());
        
        return response;
    }

    /**
     * 创建或更新模板
     */
    @PostMapping("/templates")
    public Map<String, Object> createOrUpdateTemplate(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String title = request.get("title");
        String template = request.get("template");
        String category = request.get("category");
        String description = request.get("description");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            PromptTemplate saved = promptService.createOrUpdateTemplate(
                name, title, template, category, description
            );
            
            response.put("success", true);
            response.put("data", saved);
            response.put("message", "模板保存成功");
            
        } catch (Exception e) {
            log.error("保存模板失败", e);
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 渲染模板（替换变量）
     */
    @PostMapping("/render")
    public Map<String, Object> renderPrompt(@RequestBody Map<String, Object> request) {
        String templateName = (String) request.get("templateName");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String rendered = promptService.renderPrompt(templateName, variables);
            
            response.put("success", true);
            response.put("rendered", rendered);
            response.put("templateName", templateName);
            
        } catch (Exception e) {
            log.error("渲染模板失败", e);
            response.put("success", false);
            response.put("message", "渲染失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 删除模板（软删除）
     */
    @DeleteMapping("/templates/{name}")
    public Map<String, Object> deleteTemplate(@PathVariable String name) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            promptService.deleteTemplate(name);
            
            response.put("success", true);
            response.put("message", "模板已删除");
            
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
        } catch (Exception e) {
            log.error("删除模板失败", e);
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 重新初始化默认模板
     */
    @PostMapping("/init")
    public Map<String, Object> initTemplates() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            promptService.createDefaultTemplates();
            
            response.put("success", true);
            response.put("message", "默认模板初始化成功");
            
        } catch (Exception e) {
            log.error("初始化模板失败", e);
            response.put("success", false);
            response.put("message", "初始化失败: " + e.getMessage());
        }
        
        return response;
    }
}
