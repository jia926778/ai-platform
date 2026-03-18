package com.example.aiplatform.es;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.LocalDateTime;

/**
 * AI请求日志ES文档
 * <p>
 * 对应Elasticsearch索引 ai_request_log，支持请求内容全文检索和聚合分析。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "ai_request_log")
@Setting(shards = 2, replicas = 1)
public class AiRequestLogDocument {

    @Id
    private String id;

    /** MySQL中的记录ID */
    @Field(type = FieldType.Long)
    private Long mysqlId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Keyword)
    private String apiType;

    @Field(type = FieldType.Keyword)
    private String model;

    @Field(type = FieldType.Keyword)
    private String provider;

    /** 请求内容 - 使用text类型支持全文检索，同时保留keyword用于精确匹配 */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 256)
        }
    )
    private String requestContent;

    /** 响应内容 - 支持全文检索 */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 256)
        }
    )
    private String responseContent;

    @Field(type = FieldType.Integer)
    private Integer totalTokens;

    @Field(type = FieldType.Long)
    private Long durationMs;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String errorMessage;

    @Field(type = FieldType.Keyword)
    private String clientIp;

    @Field(type = FieldType.Long)
    private Long conversationId;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;
}
