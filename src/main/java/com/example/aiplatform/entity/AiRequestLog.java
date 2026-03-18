package com.example.aiplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * AI请求日志实体类
 * <p>
 * 记录所有AI请求与响应的完整内容，用于追溯与分析。
 * 同时存储到MySQL（结构化查询）和Elasticsearch（全文检索）。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_request_log", indexes = {
    @Index(name = "idx_request_user_id", columnList = "userId"),
    @Index(name = "idx_request_api_type", columnList = "apiType"),
    @Index(name = "idx_request_created_at", columnList = "createdAt"),
    @Index(name = "idx_request_status", columnList = "status")
})
public class AiRequestLog {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户ID */
    @Column(nullable = false)
    private Long userId;

    /** 用户名（冗余字段，方便查询） */
    @Column(length = 50)
    private String username;

    /** API类型：CHAT / SUMMARY / CODEGEN */
    @Column(nullable = false, length = 30)
    private String apiType;

    /** 使用的AI模型 */
    @Column(length = 50)
    private String model;

    /** AI服务提供商：OPENAI / CLAUDE / QIANWEN 等 */
    @Column(length = 30)
    private String provider;

    /** 请求内容（用户输入） */
    @Column(columnDefinition = "MEDIUMTEXT")
    private String requestContent;

    /** 响应内容（AI输出） */
    @Column(columnDefinition = "MEDIUMTEXT")
    private String responseContent;

    /** 系统提示词 */
    @Column(columnDefinition = "TEXT")
    private String systemPrompt;

    /** 提示词Token数 */
    private Integer promptTokens;

    /** 生成内容Token数 */
    private Integer completionTokens;

    /** 总Token消耗 */
    private Integer totalTokens;

    /** 请求耗时（毫秒） */
    private Long durationMs;

    /** 请求状态：SUCCESS / FAILED */
    @Column(nullable = false, length = 20)
    private String status;

    /** 错误信息 */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /** 客户端IP地址 */
    @Column(length = 50)
    private String clientIp;

    /** 会话ID（聊天场景） */
    private Long conversationId;

    /** 请求创建时间 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
