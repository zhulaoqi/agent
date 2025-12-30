package com.kinch.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 消息实体
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
@Entity
@Table(name = "t_message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话ID
     */
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 消息角色（user-用户，assistant-助手，system-系统，tool-工具）
     */
    @Column(length = 20, nullable = false)
    private String role;

    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 消息类型（text-文本，image-图片，file-文件，tool_call-工具调用）
     */
    @Column(length = 50)
    private String type = "text";

    /**
     * Token消耗量
     */
    @Column(name = "token_count")
    private Integer tokenCount = 0;

    /**
     * 附加数据（JSON格式，存储图片URL、文件路径、工具调用参数等）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * 父消息ID（用于人工介入等场景）
     */
    @Column(name = "parent_message_id")
    private Long parentMessageId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
