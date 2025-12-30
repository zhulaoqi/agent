package com.kinch.agent.repository;

import com.kinch.agent.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提示词模板Repository
 * 
 * @author kinch
 * @date 2025-12-29
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    /**
     * 根据名称查询
     */
    Optional<PromptTemplate> findByName(String name);

    /**
     * 根据分类查询启用的模板
     */
    List<PromptTemplate> findByCategoryAndStatus(String category, String status);

    /**
     * 查询所有启用的模板
     */
    List<PromptTemplate> findByStatus(String status);

    /**
     * 查询系统内置模板
     */
    List<PromptTemplate> findByIsSystemTrue();

    /**
     * 根据标签搜索（JSON包含查询）
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.tags LIKE %:tag% AND p.status = 'active'")
    List<PromptTemplate> findByTag(@Param("tag") String tag);

    /**
     * 模糊搜索（名称或描述）
     */
    @Query("SELECT p FROM PromptTemplate p WHERE " +
           "(p.name LIKE %:keyword% OR p.description LIKE %:keyword% OR p.title LIKE %:keyword%) " +
           "AND p.status = 'active'")
    List<PromptTemplate> search(@Param("keyword") String keyword);

    /**
     * 增加使用次数
     */
    @Modifying
    @Query("UPDATE PromptTemplate p SET p.usageCount = p.usageCount + 1 WHERE p.id = :id")
    void incrementUsageCount(@Param("id") Long id);

    /**
     * 统计分类下的模板数量
     */
    Long countByCategory(String category);

    /**
     * 查询热门模板（按使用次数排序）
     */
    List<PromptTemplate> findTop10ByStatusOrderByUsageCountDesc(String status);
}



