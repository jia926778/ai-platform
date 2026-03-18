package com.example.aiplatform.repository;

import com.example.aiplatform.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天消息数据访问接口
 * <p>
 * 继承 JpaRepository 提供 ChatMessage 实体的基本 CRUD 操作，
 * 支持按会话ID查询消息列表和统计消息数量。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 查询指定会话的所有消息，按创建时间升序排列
     *
     * @param conversationId 会话ID
     * @return 消息列表（按时间顺序排列）
     */
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * 统计指定会话的消息总数
     *
     * @param conversationId 会话ID
     * @return 消息数量
     */
    long countByConversationId(Long conversationId);
}
