package com.kinch.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 会话实体
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
@Entity
@Table(name = "t_conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * ThreadID（用于Agent记忆持久化）
     */
    @Column(name = "thread_id", length = 100, unique = true)
    private String threadId;

    /**
     * 会话标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 会话类型（chat-对话，image-文生图，ppt-PPT生成，workflow-工作流）
     */
    @Column(length = 50)
    private String type = "chat";

    /**
     * Agent名称
     */
    @Column(name = "agent_name", length = 100)
    private String agentName;

    /**
     * 会话状态（active-活跃，archived-归档，deleted-已删除）
     */
    @Column(length = 20)
    private String status = "active";

    /**
     * Token使用量
     */
    @Column(name = "token_usage")
    private Long tokenUsage = 0L;

    /**
     * 消息数量
     */
    @Column(name = "message_count")
    private Integer messageCount = 0;

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
