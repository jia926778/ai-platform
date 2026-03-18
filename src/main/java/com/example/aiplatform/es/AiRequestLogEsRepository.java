package com.example.aiplatform.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * AI请求日志Elasticsearch数据访问层
 */
@Repository
public interface AiRequestLogEsRepository extends ElasticsearchRepository<AiRequestLogDocument, String> {
}
