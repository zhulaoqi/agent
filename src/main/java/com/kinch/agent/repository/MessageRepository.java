package com.kinch.agent.repository;

import com.kinch.agent.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息Repository
 *
 * @author kinch
 * @date 2025-12-29
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreateTimeAsc(Long conversationId);
    List<Message> findTop10ByConversationIdOrderByCreateTimeDesc(Long conversationId);
}
