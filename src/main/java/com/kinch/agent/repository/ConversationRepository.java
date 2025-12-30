package com.kinch.agent.repository;

import com.kinch.agent.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会话Repository
 *
 * @author kinch
 * @date 2025-12-29
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByThreadId(String threadId);
    List<Conversation> findByUserIdAndStatusOrderByUpdateTimeDesc(Long userId, String status);
    List<Conversation> findByUserIdOrderByUpdateTimeDesc(Long userId);
}
