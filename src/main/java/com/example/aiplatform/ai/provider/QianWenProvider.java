package com.example.aiplatform.ai.provider;

import com.example.aiplatform.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 通义千问 (Alibaba Cloud) AI服务提供商实现
 * <p>
 * 预留通义千问API集成接口，当前为模拟实现。
 * 展示多平台适配架构的扩展能力。
 * </p>
 */
@Slf4j
@Component
public class QianWenProvider implements AiProvider {

    @Value("${app.ai.qianwen.api-key:}")
    private String apiKey;

    @Value("${app.ai.qianwen.enabled:false}")
    private boolean enabled;

    @Override
    public String getProviderName() {
        return "QIANWEN";
    }

    @Override
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public AiResponse chat(AiRequest request) {
        if (!isAvailable()) {
            return AiResponse.builder()
                    .success(false)
                    .provider(getProviderName())
                    .errorMessage("通义千问服务未启用，请配置API密钥")
                    .build();
        }
        // TODO: 集成通义千问SDK实现实际调用
        // 当前返回模拟响应，展示架构扩展能力
        log.info("通义千问API调用（模拟）: model={}", request.getModel());
        return AiResponse.builder()
                .content("[通义千问模拟响应] 收到您的请求：" + request.getUserMessage())
                .model(request.getModel() != null ? request.getModel() : "qwen-turbo")
                .provider(getProviderName())
                .promptTokens(0)
                .completionTokens(0)
                .totalTokens(0)
                .success(true)
                .build();
    }

    @Override
    @Async("aiTaskExecutor")
    public CompletableFuture<AiResponse> chatAsync(AiRequest request) {
        return CompletableFuture.completedFuture(chat(request));
    }

    @Override
    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList("qwen-turbo", "qwen-plus", "qwen-max");
    }
}
