package com.kinch.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Token使用日志（用于成本分析和优化）
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
@Entity
@Table(name = "t_token_usage_log")
public class TokenUsageLog {

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
     * 操作类型（chat-对话，image-文生图，function-函数调用）
     */
    @Column(length = 50)
    private String operationType;

    /**
     * 模型名称
     */
    @Column(length = 100)
    private String modelName;

    /**
     * 输入Token数
     */
    @Column(name = "input_tokens")
    private Integer inputTokens = 0;

    /**
     * 输出Token数
     */
    @Column(name = "output_tokens")
    private Integer outputTokens = 0;

    /**
     * 总Token数
     */
    @Column(name = "total_tokens")
    private Integer totalTokens = 0;

    /**
     * 预估成本（元）
     */
    @Column(name = "estimated_cost")
    private Double estimatedCost = 0.0;

    /**
     * 请求耗时（毫秒）
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * 是否超出限制
     */
    @Column(name = "exceeded_limit")
    private Boolean exceededLimit = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
