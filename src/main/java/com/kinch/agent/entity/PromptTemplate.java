package com.kinch.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 提示词模板实体
 * 支持持久化存储，避免重启丢失
 * 
 * @author kinch
 * @date 2025-12-29
 */
@Data
@Entity
@Table(name = "t_prompt_template", 
       indexes = {
           @Index(name = "idx_name", columnList = "name", unique = true),
           @Index(name = "idx_category", columnList = "category"),
           @Index(name = "idx_status", columnList = "status")
       })
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板名称（唯一）
     */
    @Column(length = 100, nullable = false, unique = true)
    private String name;

    /**
     * 模板标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 模板内容（支持{variable}占位符）
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String template;

    /**
     * 模板描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 模板分类（代码生成、需求分析、测试、文档等）
     */
    @Column(length = 50)
    private String category;

    /**
     * 版本号
     */
    @Column(nullable = false)
    private Integer version = 1;

    /**
     * 状态（active-启用，inactive-禁用，draft-草稿）
     */
    @Column(length = 20, nullable = false)
    private String status = "active";

    /**
     * 变量列表（JSON格式）
     * 例如: ["language", "code", "description"]
     */
    @Column(columnDefinition = "TEXT")
    private String variables;

    /**
     * 使用次数统计
     */
    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    /**
     * 是否系统内置模板
     */
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    /**
     * 创建人ID
     */
    @Column(name = "creator_id")
    private Long creatorId;

    /**
     * 最后修改人ID
     */
    @Column(name = "updater_id")
    private Long updaterId;

    /**
     * 标签（JSON数组，用于搜索）
     * 例如: ["java", "spring", "backend"]
     */
    @Column(columnDefinition = "TEXT")
    private String tags;

    /**
     * 扩展配置（JSON格式）
     * 可存储：默认值、校验规则、示例等
     */
    @Column(columnDefinition = "TEXT")
    private String config;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}



