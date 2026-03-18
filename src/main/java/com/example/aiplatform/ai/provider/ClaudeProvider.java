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
 * Claude (Anthropic) AI服务提供商实现
 * <p>
 * 预留Claude API集成接口，当前为模拟实现。
 * 展示多平台适配架构的扩展能力。
 * </p>
 */
@Slf4j
@Component
public class ClaudeProvider implements AiProvider {

    @Value("${app.ai.claude.api-key:}")
    private String apiKey;

    @Value("${app.ai.claude.enabled:false}")
    private boolean enabled;

    @Override
    public String getProviderName() {
        return "CLAUDE";
    }

    @Override
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public AiResponse chat(AiRequest request) {
        if (!isAvailable()) {
            return AiResponse.builder()
                    .success(false)
                    .provider(getProviderName())
                    .errorMessage("Claude服务未启用，请配置API密钥")
                    .build();
        }
        // TODO: 集成Anthropic SDK实现实际调用
        // 当前返回模拟响应，展示架构扩展能力
        log.info("Claude API调用（模拟）: model={}", request.getModel());
        return AiResponse.builder()
                .content("[Claude模拟响应] 收到您的请求：" + request.getUserMessage())
                .model(request.getModel() != null ? request.getModel() : "claude-3-sonnet")
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
        return Arrays.asList("claude-3-opus", "claude-3-sonnet", "claude-3-haiku");
    }
}
