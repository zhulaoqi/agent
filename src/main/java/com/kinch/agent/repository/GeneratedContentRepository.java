package com.kinch.agent.repository;

import com.kinch.agent.entity.GeneratedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 生成内容Repository
 *
 * @author kinch
 * @date 2025-12-29
 */
@Repository
public interface GeneratedContentRepository extends JpaRepository<GeneratedContent, Long> {

    /**
     * 查询用户的所有生成内容
     *
     * @param userId 用户ID
     * @return 生成内容列表
     */
    List<GeneratedContent> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 根据类型查询用户的生成内容
     *
     * @param userId      用户ID
     * @param contentType 内容类型
     * @return 生成内容列表
     */
    List<GeneratedContent> findByUserIdAndContentTypeOrderByCreateTimeDesc(Long userId, String contentType);

    /**
     * 查询会话的生成内容
     *
     * @param conversationId 会话ID
     * @return 生成内容列表
     */
    List<GeneratedContent> findByConversationIdOrderByCreateTimeDesc(Long conversationId);
}

