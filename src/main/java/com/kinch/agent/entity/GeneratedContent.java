package com.kinch.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 生成内容实体（保存用户生成的图片、PPT、脚本等）
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
@Entity
@Table(name = "t_generated_content")
public class GeneratedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 会话ID
     */
    @Column(name = "conversation_id")
    private Long conversationId;

    /**
     * 内容类型（image-图片，ppt-PPT，script-脚本，material-素材）
     */
    @Column(length = 50, nullable = false)
    private String contentType;

    /**
     * 内容标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 内容描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 文件路径或URL
     */
    @Column(length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 生成参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /**
     * 状态（success-成功，failed-失败，processing-处理中）
     */
    @Column(length = 20)
    private String status = "processing";

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
