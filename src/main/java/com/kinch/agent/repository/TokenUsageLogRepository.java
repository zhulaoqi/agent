package com.kinch.agent.repository;

import com.kinch.agent.entity.TokenUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Token使用日志Repository
 *
 * @author kinch
 * @date 2025-12-29
 */
@Repository
public interface TokenUsageLogRepository extends JpaRepository<TokenUsageLog, Long> {

    /**
     * 查询用户在指定时间范围内的Token使用记录
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return Token使用记录列表
     */
    List<TokenUsageLog> findByUserIdAndCreateTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户今日Token使用总量
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @return Token总量
     */
    @Query("SELECT COALESCE(SUM(t.totalTokens), 0) FROM TokenUsageLog t WHERE t.userId = :userId AND t.createTime >= :startTime")
    Long sumTodayTokenUsage(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
}

