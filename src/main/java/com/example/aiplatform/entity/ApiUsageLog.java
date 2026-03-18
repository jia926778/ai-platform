package com.example.aiplatform.entity;

/**
 * API调用日志实体类
 * <p>
 * 对应数据库表 api_usage_log，记录每次AI API调用的详细信息，
 * 包括调用类型、使用的模型、token消耗、费用和调用状态。
 * 用于使用量统计、费用核算和异常追踪。
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_usage_log")
public class ApiUsageLog {

    /** 日志唯一标识（主键，自增） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 调用API的用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** API类型（如：chat、summary、code_gen） */
    @Column(name = "api_type", nullable = false, length = 50)
    private String apiType;

    /** 使用的AI模型名称 */
    @Column(length = 50)
    private String model;

    /** 提示词（输入）消耗的token数量 */
    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    /** 补全（输出）消耗的token数量 */
    @Column(name = "completion_tokens")
    private Integer completionTokens;

    /** 总共消耗的token数量 */
    @Column(name = "total_tokens")
    private Integer totalTokens;

    /** 本次调用的费用（精度：10位整数，6位小数） */
    @Column(precision = 10, scale = 6)
    private BigDecimal cost;

    /** 调用状态（如：success、error） */
    @Column(length = 20)
    private String status;

    /** 错误信息（调用失败时记录） */
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    /** 日志记录时间（不可更新） */
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
