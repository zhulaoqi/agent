package com.kinch.agent.service;

import com.google.gson.Gson;
import com.kinch.agent.entity.PromptTemplate;
import com.kinch.agent.repository.PromptTemplateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 提示词管理服务（专业版 - 数据库持久化）
 * 
 * 【存储方案】：
 * 1. MySQL数据库 - 主存储（持久化）
 * 2. Redis缓存 - 二级缓存（性能优化）
 * 3. 支持热更新、版本控制、使用统计
 * 
 * 【行业最佳实践】：
 * - 数据库持久化避免重启丢失
 * - Redis缓存提升查询性能
 * - 版本控制支持回滚
 * - 使用统计支持优化
 * 
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptManagementServiceV2 {

    private final PromptTemplateRepository repository;
    private final RedisTemplate<String, String> redisTemplate;
    private final Gson gson;
    
    private static final String CACHE_PREFIX = "prompt:";
    private static final long CACHE_TTL = 3600; // 1小时

    /**
     * 启动时初始化默认模板
     */
    @PostConstruct
    public void initDefaultTemplates() {
        log.info("检查并初始化默认提示词模板");
        
        // 只在数据库为空时初始化
        if (repository.count() == 0) {
            log.info("数据库为空，开始初始化默认模板");
            createDefaultTemplates();
        } else {
            log.info("数据库已有模板，跳过初始化。现有模板数: {}", repository.count());
        }
    }

    /**
     * 创建默认模板
     */
    @Transactional
    public void createDefaultTemplates() {
        // 1. 代码审查
        createSystemTemplate(
            "code_review",
            "代码审查助手",
            """
            请审查以下{language}代码，重点关注：
            1. 代码规范性
            2. 性能问题
            3. 安全隐患
            4. 最佳实践
            
            代码：
            ```{language}
            {code}
            ```
            
            请给出详细的审查意见。
            """,
            "开发工具",
            "对代码进行全面审查，包括规范、性能、安全等方面",
            List.of("language", "code")
        );

        // 2. 需求分析
        createSystemTemplate(
            "requirement_analysis",
            "需求分析专家",
            """
            作为产品经理，请分析以下需求：
            
            需求描述：{requirement}
            
            请从以下角度分析：
            1. 用户价值
            2. 技术可行性
            3. 优先级建议
            4. 潜在风险
            
            请给出专业的分析报告。
            """,
            "产品管理",
            "分析产品需求，评估价值和可行性",
            List.of("requirement")
        );

        // 3. 代码生成
        createSystemTemplate(
            "code_generation",
            "代码生成器",
            """
            请用{language}语言实现以下功能：
            
            功能描述：{description}
            
            要求：
            1. 代码规范
            2. 包含注释
            3. 错误处理
            4. 遵循{language}最佳实践
            
            请直接输出代码。
            """,
            "代码生成",
            "根据需求描述生成规范的代码",
            List.of("language", "description")
        );

        // 4. Bug诊断
        createSystemTemplate(
            "bug_diagnosis",
            "Bug诊断专家",
            """
            请帮助诊断以下Bug：
            
            Bug描述：{description}
            错误信息：{error_message}
            相关代码：{code}
            
            请提供：
            1. 可能原因分析
            2. 解决方案建议
            3. 预防措施
            """,
            "问题诊断",
            "诊断Bug原因并提供解决方案",
            List.of("description", "error_message", "code")
        );

        // 5. API文档生成
        createSystemTemplate(
            "api_doc",
            "API文档生成器",
            """
            请为以下API生成文档：
            
            API名称：{api_name}
            功能：{function}
            请求方法：{method}
            请求参数：{params}
            响应格式：{response}
            
            请按照以下格式生成文档：
            1. 接口概述
            2. 请求说明
            3. 响应说明
            4. 错误码说明
            5. 调用示例
            """,
            "文档生成",
            "自动生成API接口文档",
            List.of("api_name", "function", "method", "params", "response")
        );

        // 6. 测试用例生成
        createSystemTemplate(
            "test_case",
            "测试用例生成器",
            """
            请为以下功能生成测试用例：
            
            功能描述：{function}
            输入参数：{inputs}
            预期输出：{expected_output}
            
            请生成：
            1. 正常场景测试
            2. 边界条件测试
            3. 异常场景测试
            
            测试框架：{framework}
            """,
            "测试",
            "生成全面的测试用例",
            List.of("function", "inputs", "expected_output", "framework")
        );

        // 7. 命名建议
        createSystemTemplate(
            "naming",
            "命名助手",
            """
            请为以下概念提供编程命名建议：
            
            中文描述：{chinese_name}
            用途：{usage}
            
            请提供以下格式的命名：
            1. 类名（PascalCase）
            2. 变量名（camelCase）
            3. 常量名（UPPER_SNAKE_CASE）
            4. 表名（snake_case）
            5. 接口名（I前缀）
            6. 方法名（动词开头）
            
            要求：语义清晰、符合规范、避免歧义
            """,
            "命名工具",
            "提供符合规范的编程命名建议",
            List.of("chinese_name", "usage")
        );

        // 8. 技术方案设计
        createSystemTemplate(
            "tech_design",
            "技术方案设计",
            """
            请为以下需求设计技术方案：
            
            需求：{requirement}
            技术栈：{tech_stack}
            约束条件：{constraints}
            
            请提供：
            1. 架构设计
            2. 技术选型理由
            3. 实施步骤
            4. 风险评估
            """,
            "架构设计",
            "设计完整的技术实施方案",
            List.of("requirement", "tech_stack", "constraints")
        );

        log.info("默认模板初始化完成，共创建8个模板");
    }

    /**
     * 创建系统模板
     */
    private void createSystemTemplate(String name, String title, String template, 
                                     String category, String description, List<String> variables) {
        if (repository.findByName(name).isPresent()) {
            log.info("模板已存在，跳过: {}", name);
            return;
        }

        PromptTemplate entity = new PromptTemplate();
        entity.setName(name);
        entity.setTitle(title);
        entity.setTemplate(template);
        entity.setCategory(category);
        entity.setDescription(description);
        entity.setVariables(gson.toJson(variables));
        entity.setIsSystem(true);
        entity.setStatus("active");
        entity.setVersion(1);
        
        repository.save(entity);
        log.info("创建系统模板: {}", name);
    }

    /**
     * 根据名称获取模板（带缓存）
     */
    @Cacheable(value = "promptTemplates", key = "#name")
    public Optional<PromptTemplate> getTemplate(String name) {
        log.info("查询提示词模板: {}", name);
        
        // 1. 先查Redis缓存
        String cacheKey = CACHE_PREFIX + name;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从Redis缓存命中: {}", name);
            return Optional.of(gson.fromJson(cached, PromptTemplate.class));
        }

        // 2. 查数据库
        Optional<PromptTemplate> template = repository.findByName(name);
        
        // 3. 写入Redis缓存
        template.ifPresent(t -> {
            redisTemplate.opsForValue().set(cacheKey, gson.toJson(t), CACHE_TTL, TimeUnit.SECONDS);
            log.info("写入Redis缓存: {}", name);
        });
        
        return template;
    }

    /**
     * 渲染提示词（替换变量）
     */
    @Transactional
    public String renderPrompt(String templateName, Map<String, Object> variables) {
        log.info("渲染提示词 - 模板: {}, 变量数: {}", templateName, variables.size());
        
        PromptTemplate template = getTemplate(templateName)
            .orElseThrow(() -> new IllegalArgumentException("模板不存在: " + templateName));
        
        // 替换变量
        String rendered = template.getTemplate();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            rendered = rendered.replace(placeholder, String.valueOf(entry.getValue()));
        }
        
        // 增加使用次数
        repository.incrementUsageCount(template.getId());
        
        return rendered;
    }

    /**
     * 创建Prompt对象
     */
    public Prompt createPrompt(String templateName, Map<String, Object> variables) {
        String rendered = renderPrompt(templateName, variables);
        return new Prompt(rendered);
    }

    /**
     * 创建或更新模板
     */
    @Transactional
    @CacheEvict(value = "promptTemplates", key = "#name")
    public PromptTemplate createOrUpdateTemplate(String name, String title, String template,
                                                 String category, String description) {
        Optional<PromptTemplate> existing = repository.findByName(name);
        
        PromptTemplate entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setTitle(title);
            entity.setTemplate(template);
            entity.setCategory(category);
            entity.setDescription(description);
            entity.setVersion(entity.getVersion() + 1);
            log.info("更新模板: {}, 新版本: {}", name, entity.getVersion());
        } else {
            entity = new PromptTemplate();
            entity.setName(name);
            entity.setTitle(title);
            entity.setTemplate(template);
            entity.setCategory(category);
            entity.setDescription(description);
            entity.setStatus("active");
            entity.setIsSystem(false);
            log.info("创建新模板: {}", name);
        }
        
        PromptTemplate saved = repository.save(entity);
        
        // 清除Redis缓存
        redisTemplate.delete(CACHE_PREFIX + name);
        
        return saved;
    }

    /**
     * 列出所有启用的模板
     */
    public List<PromptTemplate> listActiveTemplates() {
        return repository.findByStatus("active");
    }

    /**
     * 按分类列出模板
     */
    public List<PromptTemplate> listByCategory(String category) {
        return repository.findByCategoryAndStatus(category, "active");
    }

    /**
     * 搜索模板
     */
    public List<PromptTemplate> search(String keyword) {
        return repository.search(keyword);
    }

    /**
     * 获取热门模板
     */
    public List<PromptTemplate> getPopularTemplates() {
        return repository.findTop10ByStatusOrderByUsageCountDesc("active");
    }

    /**
     * 删除模板（软删除）
     */
    @Transactional
    @CacheEvict(value = "promptTemplates", key = "#name")
    public void deleteTemplate(String name) {
        PromptTemplate template = repository.findByName(name)
            .orElseThrow(() -> new IllegalArgumentException("模板不存在: " + name));
        
        if (template.getIsSystem()) {
            throw new IllegalStateException("系统模板不能删除");
        }
        
        template.setStatus("inactive");
        repository.save(template);
        
        // 清除缓存
        redisTemplate.delete(CACHE_PREFIX + name);
        
        log.info("删除模板: {}", name);
    }
}


