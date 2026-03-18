package com.example.aiplatform.entity;

/**
 * 聊天消息实体类
 * <p>
 * 对应数据库表 chat_message，存储聊天会话中的单条消息。
 * 包含消息角色（user/assistant/system）、消息内容和token用量等信息。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    /** 消息唯一标识（主键，自增） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属会话ID */
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /** 消息角色：user（用户）、assistant（AI助手）、system（系统） */
    @Column(nullable = false, length = 20)
    private String role;

    /** 消息文本内容 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 该消息消耗的token数量 */
    @Column
    private Integer tokens;

    /** 消息创建时间（不可更新） */
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
