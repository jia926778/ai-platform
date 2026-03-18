package com.example.aiplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI平台应用程序启动类
 * <p>
 * Spring Boot应用的主入口类。
 * 通过 {@code @SpringBootApplication} 注解启用自动配置、组件扫描和属性加载。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@SpringBootApplication
public class AiPlatformApplication {

    /**
     * 应用程序主方法（入口点）
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AiPlatformApplication.class, args);
    }
}
