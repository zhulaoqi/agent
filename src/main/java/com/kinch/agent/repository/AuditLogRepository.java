package com.kinch.agent.repository;

import com.kinch.agent.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Repository
 *
 * @author kinch
 * @date 2025-12-29
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 查询用户的审计日志
     */
    List<AuditLog> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 查询时间范围内的审计日志
     */
    List<AuditLog> findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询需要审核的日志
     */
    List<AuditLog> findByRequireReviewTrueOrderByCreateTimeDesc();
    
    /**
     * 获取最近的日志
     */
    List<AuditLog> findTop10ByOrderByCreateTimeDesc();
}


