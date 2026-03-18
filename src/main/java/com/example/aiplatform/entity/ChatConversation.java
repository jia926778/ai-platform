package com.example.aiplatform.entity;

/**
 * 聊天会话实体类
 * <p>
 * 对应数据库表 chat_conversation，存储用户的聊天会话信息。
 * 每个会话包含标题、所使用的AI模型以及时间戳信息。
 * 通过 {@code @Transient} 注解持有消息列表（不持久化到数据库）。
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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_conversation")
public class ChatConversation {

    /** 会话唯一标识（主键，自增） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 会话标题（通常由首条消息自动生成） */
    @Column(length = 200)
    private String title;

    /** 使用的AI模型名称，默认为 gpt-3.5-turbo */
    @Column(length = 50)
    @Builder.Default
    private String model = "gpt-3.5-turbo";

    /** 会话创建时间（不可更新） */
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 会话最后更新时间 */
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** 会话中的消息列表（非持久化字段，运行时加载） */
    @Transient
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();
}
