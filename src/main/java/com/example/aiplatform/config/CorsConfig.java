package com.example.aiplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域资源共享（CORS）配置类
 * <p>
 * 配置全局CORS策略，允许所有来源、所有HTTP方法和所有请求头的跨域请求。
 * 支持携带凭据（Cookie等），预检请求缓存时间为1小时。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * 创建CORS过滤器Bean
     * <p>
     * 对所有路径（/**）应用CORS配置，允许前端跨域调用后端API。
     * </p>
     *
     * @return CORS过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");    // 允许所有来源
        config.addAllowedMethod("*");           // 允许所有HTTP方法
        config.addAllowedHeader("*");           // 允许所有请求头
        config.setAllowCredentials(true);       // 允许携带凭据
        config.setMaxAge(3600L);                // 预检请求缓存1小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
