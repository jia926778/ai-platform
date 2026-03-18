package com.example.aiplatform.repository;

import com.example.aiplatform.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天会话数据访问接口
 * <p>
 * 继承 JpaRepository 提供 ChatConversation 实体的基本 CRUD 操作，
 * 支持按用户ID查询会话列表和统计会话数量。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    /**
     * 查询指定用户的所有会话，按最后更新时间降序排列
     *
     * @param userId 用户ID
     * @return 会话列表（最新的排在前面）
     */
    List<ChatConversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /**
     * 统计指定用户的会话总数
     *
     * @param userId 用户ID
     * @return 会话数量
     */
    long countByUserId(Long userId);
}
