package com.kinch.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 工作流执行记录（基于Graph Core的工作流编排）
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
@Entity
@Table(name = "t_workflow_execution")
public class WorkflowExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 工作流ID
     */
    @Column(name = "workflow_id", length = 100)
    private String workflowId;

    /**
     * 工作流名称
     */
    @Column(length = 100)
    private String workflowName;

    /**
     * 工作流定义（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String workflowDefinition;

    /**
     * 执行状态（pending-待执行，running-执行中，paused-暂停，completed-完成，failed-失败）
     */
    @Column(length = 20)
    private String status = "pending";

    /**
     * 当前步骤
     */
    @Column(name = "current_step", length = 100)
    private String currentStep;

    /**
     * 总步骤数
     */
    @Column(name = "total_steps")
    private Integer totalSteps = 0;

    /**
     * 已完成步骤数
     */
    @Column(name = "completed_steps")
    private Integer completedSteps = 0;

    /**
     * 执行结果（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String result;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 是否需要人工介入
     */
    @Column(name = "require_human_intervention")
    private Boolean requireHumanIntervention = false;

    /**
     * 人工介入说明
     */
    @Column(columnDefinition = "TEXT")
    private String interventionNote;

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

    /**
     * 完成时间
     */
    @Column(name = "complete_time")
    private LocalDateTime completeTime;
}
