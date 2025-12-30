package com.kinch.agent.repository;

import com.kinch.agent.entity.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流执行Repository
 *
 * @author kinch
 * @date 2025-12-29
 */
@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long> {

    /**
     * 查询用户的所有工作流执行记录
     *
     * @param userId 用户ID
     * @return 工作流执行记录列表
     */
    List<WorkflowExecution> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 查询需要人工介入的工作流
     *
     * @param userId 用户ID
     * @return 工作流执行记录列表
     */
    List<WorkflowExecution> findByUserIdAndRequireHumanInterventionTrueOrderByCreateTimeDesc(Long userId);

    /**
     * 查询指定状态的工作流
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 工作流执行记录列表
     */
    List<WorkflowExecution> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, String status);
}

