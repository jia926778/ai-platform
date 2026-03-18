package com.example.aiplatform.ratelimit;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类
 * <p>
 * 注册限流拦截器，对AI相关的API路径（聊天、摘要、代码生成）进行限流保护。
 * 其他路径（如认证、用户管理）不受限流限制。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    /**
     * 注册拦截器并配置拦截路径
     * <p>
     * 仅对AI功能相关的API路径应用限流拦截器。
     * </p>
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/chat/**", "/api/summary/**", "/api/codegen/**");
    }
}
