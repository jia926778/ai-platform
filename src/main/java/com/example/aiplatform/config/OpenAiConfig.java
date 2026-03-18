package com.example.aiplatform.config;

import org.springframework.context.annotation.Configuration;

/**
 * OpenAI配置类
 * <p>
 * Spring AI 通过 application.yml 自动配置 OpenAI 客户端。
 * 如需自定义配置（如自定义HTTP客户端、重试策略等），可在此类中添加Bean。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Configuration
public class OpenAiConfig {
    // Spring AI通过application.yml自动配置OpenAI客户端
    // 如果需要额外的自定义配置，可以在此处添加Bean
}
