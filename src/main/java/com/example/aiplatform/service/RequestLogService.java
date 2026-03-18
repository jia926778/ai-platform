package com.example.aiplatform.service;

import com.example.aiplatform.dto.FrequentQuestionDTO;
import com.example.aiplatform.dto.RequestLogQueryDTO;
import com.example.aiplatform.dto.RequestLogResponse;
import com.example.aiplatform.entity.AiRequestLog;
import com.example.aiplatform.es.AiRequestLogDocument;
import com.example.aiplatform.es.AiRequestLogEsRepository;
import com.example.aiplatform.exception.BusinessException;
import com.example.aiplatform.repository.AiRequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.json.JsonData;

/**
 * AI请求日志服务
 * <p>
 * 提供请求日志的记录、查询、全文检索和聚合分析功能。
 * MySQL用于结构化存储和基础查询，Elasticsearch用于全文检索和数据分析。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestLogService {

    private final AiRequestLogRepository requestLogRepository;
    private final AiRequestLogEsRepository esRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 记录AI请求日志
     * <p>同步写入MySQL，异步索引到Elasticsearch</p>
     */
    @Transactional
    public AiRequestLog logRequest(AiRequestLog requestLog) {
        // 保存到MySQL
        AiRequestLog saved = requestLogRepository.save(requestLog);
        // 异步索引到ES
        indexToEsAsync(saved);
        return saved;
    }

    /**
     * 异步将请求日志索引到Elasticsearch
     */
    @Async("taskExecutor")
    public void indexToEsAsync(AiRequestLog requestLog) {
        try {
            AiRequestLogDocument doc = convertToEsDocument(requestLog);
            esRepository.save(doc);
            log.debug("请求日志已索引到ES, mysqlId={}", requestLog.getId());
        } catch (Exception e) {
            // ES索引失败不影响主流程，仅记录日志
            log.warn("请求日志索引到ES失败, mysqlId={}, error={}", requestLog.getId(), e.getMessage());
        }
    }

    /**
     * 用户查看自己的历史请求（从MySQL查询）
     */
    public Page<RequestLogResponse> getUserRequests(Long userId, String apiType, Pageable pageable) {
        Page<AiRequestLog> page;
        if (apiType != null && !apiType.isEmpty()) {
            page = requestLogRepository.findByUserIdAndApiTypeOrderByCreatedAtDesc(userId, apiType, pageable);
        } else {
            page = requestLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return page.map(this::convertToResponse);
    }

    /**
     * 获取请求详情
     */
    public RequestLogResponse getRequestDetail(Long id, Long userId, boolean isAdmin) {
        AiRequestLog requestLog = requestLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "请求记录不存在"));
        // 非管理员只能查看自己的记录
        if (!isAdmin && !requestLog.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看该请求记录");
        }
        return convertToResponse(requestLog);
    }

    /**
     * 管理员按条件筛选请求（MySQL基础筛选）
     */
    public Page<RequestLogResponse> adminFilterRequests(RequestLogQueryDTO query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        Page<AiRequestLog> page = requestLogRepository.findByFilters(
                query.getUserId(),
                query.getApiType(),
                query.getStatus(),
                query.getStartTime(),
                query.getEndTime(),
                pageable
        );
        return page.map(this::convertToResponse);
    }

    /**
     * 管理员通过ES全文检索请求日志
     * <p>支持关键词搜索、多条件筛选、时间范围过滤</p>
     */
    public List<RequestLogResponse> searchByKeyword(RequestLogQueryDTO query) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 关键词全文检索（搜索请求内容和响应内容）
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            boolQuery.must(m -> m.multiMatch(mm -> mm
                    .fields("requestContent", "responseContent")
                    .query(query.getKeyword())
                    .fuzziness("AUTO")
            ));
        }

        // 按用户ID筛选
        if (query.getUserId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("userId").value(query.getUserId())));
        }

        // 按API类型筛选
        if (query.getApiType() != null && !query.getApiType().isEmpty()) {
            boolQuery.filter(f -> f.term(t -> t.field("apiType").value(query.getApiType())));
        }

        // 按状态筛选
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            boolQuery.filter(f -> f.term(t -> t.field("status").value(query.getStatus())));
        }

        // 按提供商筛选
        if (query.getProvider() != null && !query.getProvider().isEmpty()) {
            boolQuery.filter(f -> f.term(t -> t.field("provider").value(query.getProvider())));
        }

        // 时间范围筛选
        if (query.getStartTime() != null || query.getEndTime() != null) {
            boolQuery.filter(f -> f.range(r -> {
                var rangeQuery = r.field("createdAt");
                if (query.getStartTime() != null) {
                    rangeQuery.gte(JsonData.of(query.getStartTime().toString()));
                }
                if (query.getEndTime() != null) {
                    rangeQuery.lte(JsonData.of(query.getEndTime().toString()));
                }
                return rangeQuery;
            }));
        }

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()))
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .withPageable(PageRequest.of(query.getPage(), query.getSize()))
                .build();

        SearchHits<AiRequestLogDocument> hits = elasticsearchTemplate.search(searchQuery, AiRequestLogDocument.class);

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::convertEsDocToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 高频问题统计（基于ES聚合）
     * <p>统计指定时间范围内出现频率最高的请求内容关键词</p>
     */
    public List<FrequentQuestionDTO> getFrequentQuestions(String apiType, int topN,
                                                           LocalDateTime start, LocalDateTime end) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        boolQuery.filter(f -> f.term(t -> t.field("status").value("SUCCESS")));

        if (apiType != null && !apiType.isEmpty()) {
            boolQuery.filter(f -> f.term(t -> t.field("apiType").value(apiType)));
        }
        if (start != null) {
            boolQuery.filter(f -> f.range(r -> r.field("createdAt")
                    .gte(JsonData.of(start.toString()))));
        }
        if (end != null) {
            boolQuery.filter(f -> f.range(r -> r.field("createdAt")
                    .lte(JsonData.of(end.toString()))));
        }

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()))
                .withAggregation("frequent_questions",
                        Aggregation.of(a -> a.terms(t -> t
                                .field("requestContent.keyword")
                                .size(topN)
                        ))
                )
                .withMaxResults(0) // 只要聚合结果，不需要具体文档
                .build();

        SearchHits<AiRequestLogDocument> hits = elasticsearchTemplate.search(searchQuery, AiRequestLogDocument.class);

        List<FrequentQuestionDTO> result = new ArrayList<>();
        if (hits.hasAggregations()) {
            var aggregations = hits.getAggregations();
            // 从ElasticsearchAggregations中提取terms聚合结果
            if (aggregations != null) {
                var esAggregations = ((org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations) aggregations)
                        .aggregationsAsMap();
                var termsAgg = esAggregations.get("frequent_questions");
                if (termsAgg != null) {
                    termsAgg.aggregation().getAggregate().sterms().buckets().array().forEach(bucket -> {
                        result.add(FrequentQuestionDTO.builder()
                                .keyword(bucket.key().stringValue())
                                .count(bucket.docCount())
                                .build());
                    });
                }
            }
        }
        return result;
    }

    // ========== 内部转换方法 ==========

    private AiRequestLogDocument convertToEsDocument(AiRequestLog requestLog) {
        return AiRequestLogDocument.builder()
                .id(String.valueOf(requestLog.getId()))
                .mysqlId(requestLog.getId())
                .userId(requestLog.getUserId())
                .username(requestLog.getUsername())
                .apiType(requestLog.getApiType())
                .model(requestLog.getModel())
                .provider(requestLog.getProvider())
                .requestContent(requestLog.getRequestContent())
                .responseContent(requestLog.getResponseContent())
                .totalTokens(requestLog.getTotalTokens())
                .durationMs(requestLog.getDurationMs())
                .status(requestLog.getStatus())
                .errorMessage(requestLog.getErrorMessage())
                .clientIp(requestLog.getClientIp())
                .conversationId(requestLog.getConversationId())
                .createdAt(requestLog.getCreatedAt())
                .build();
    }

    private RequestLogResponse convertToResponse(AiRequestLog requestLog) {
        return RequestLogResponse.builder()
                .id(requestLog.getId())
                .userId(requestLog.getUserId())
                .username(requestLog.getUsername())
                .apiType(requestLog.getApiType())
                .model(requestLog.getModel())
                .provider(requestLog.getProvider())
                .requestContent(requestLog.getRequestContent())
                .responseContent(requestLog.getResponseContent())
                .totalTokens(requestLog.getTotalTokens())
                .durationMs(requestLog.getDurationMs())
                .status(requestLog.getStatus())
                .errorMessage(requestLog.getErrorMessage())
                .conversationId(requestLog.getConversationId())
                .createdAt(requestLog.getCreatedAt())
                .build();
    }

    private RequestLogResponse convertEsDocToResponse(AiRequestLogDocument doc) {
        return RequestLogResponse.builder()
                .id(doc.getMysqlId())
                .userId(doc.getUserId())
                .username(doc.getUsername())
                .apiType(doc.getApiType())
                .model(doc.getModel())
                .provider(doc.getProvider())
                .requestContent(doc.getRequestContent())
                .responseContent(doc.getResponseContent())
                .totalTokens(doc.getTotalTokens())
                .durationMs(doc.getDurationMs())
                .status(doc.getStatus())
                .errorMessage(doc.getErrorMessage())
                .conversationId(doc.getConversationId())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
