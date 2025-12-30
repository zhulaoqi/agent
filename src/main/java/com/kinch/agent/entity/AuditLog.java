package com.kinch.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 审计日志实体（记录所有AI操作，用于审计和追溯）
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
@Entity
@Table(name = "t_audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 用户名
     */
    @Column(length = 50)
    private String username;

    /**
     * 会话ID
     */
    @Column(name = "conversation_id")
    private Long conversationId;

    /**
     * 操作类型（chat-对话，tool_call-工具调用，workflow-工作流，image-生成图片等）
     */
    @Column(name = "operation_type", length = 50)
    private String operationType;

    /**
     * Agent名称
     */
    @Column(name = "agent_name", length = 100)
    private String agentName;

    /**
     * 工具名称（如果是工具调用）
     */
    @Column(name = "tool_name", length = 100)
    private String toolName;

    /**
     * 输入内容
     */
    @Column(columnDefinition = "TEXT")
    private String input;

    /**
     * 输出内容
     */
    @Column(columnDefinition = "TEXT")
    private String output;

    /**
     * Token消耗
     */
    @Column(name = "token_cost")
    private Integer tokenCost = 0;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * 执行状态（success-成功，failed-失败，blocked-被拦截）
     */
    @Column(length = 20)
    private String status;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 是否需要人工审核
     */
    @Column(name = "require_review")
    private Boolean requireReview = false;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
