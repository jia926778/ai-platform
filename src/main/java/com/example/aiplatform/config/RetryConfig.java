package com.example.aiplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Spring Retry 配置
 * <p>
 * 启用基于注解的重试机制，用于AI API调用等可能因网络波动而临时失败的场景。
 * 重试策略在各Provider的 @Retryable 注解中配置。
 * </p>
 */
@Configuration
@EnableRetry
public class RetryConfig {
}
