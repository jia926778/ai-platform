package com.example.aiplatform.ai;

import com.example.aiplatform.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI服务提供商管理器
 * <p>
 * 统一管理所有注册的AI服务提供商，支持按名称或模型选择提供商。
 * 实现提供商的自动注册、路由和故障转移。
 * </p>
 */
@Slf4j
@Component
public class AiProviderManager {

    private final Map<String, AiProvider> providerMap = new HashMap<>();
    private final List<AiProvider> providers;
    /** 默认提供商名称 */
    private String defaultProviderName = "OPENAI";

    /**
     * 构造函数：自动注入所有AiProvider实现并注册
     */
    public AiProviderManager(List<AiProvider> providers) {
        this.providers = providers;
        for (AiProvider provider : providers) {
            providerMap.put(provider.getProviderName().toUpperCase(), provider);
            log.info("注册AI服务提供商: {}, 可用: {}, 支持模型: {}",
                    provider.getProviderName(), provider.isAvailable(), provider.getSupportedModels());
        }
    }

    /**
     * 获取默认提供商
     */
    public AiProvider getDefaultProvider() {
        return getProvider(defaultProviderName);
    }

    /**
     * 按名称获取提供商
     */
    public AiProvider getProvider(String providerName) {
        AiProvider provider = providerMap.get(providerName.toUpperCase());
        if (provider == null) {
            throw new BusinessException(400, "不支持的AI服务提供商: " + providerName);
        }
        if (!provider.isAvailable()) {
            throw new BusinessException(503, "AI服务提供商当前不可用: " + providerName);
        }
        return provider;
    }

    /**
     * 根据模型名称自动匹配提供商
     */
    public AiProvider getProviderByModel(String model) {
        if (model == null || model.isEmpty()) {
            return getDefaultProvider();
        }
        for (AiProvider provider : providers) {
            if (provider.isAvailable() && provider.getSupportedModels().contains(model)) {
                return provider;
            }
        }
        // 未找到匹配的提供商，使用默认
        log.warn("未找到支持模型 {} 的提供商，使用默认提供商", model);
        return getDefaultProvider();
    }

    /**
     * 获取所有可用的提供商信息
     */
    public List<Map<String, Object>> listProviders() {
        return providers.stream().map(p -> {
            Map<String, Object> info = new HashMap<>();
            info.put("name", p.getProviderName());
            info.put("available", p.isAvailable());
            info.put("models", p.getSupportedModels());
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * 带故障转移的调用
     * <p>如果指定提供商失败，自动尝试其他可用提供商</p>
     */
    public AiResponse chatWithFallback(String preferredProvider, AiRequest request) {
        // 先尝试首选提供商
        try {
            AiProvider provider = getProvider(preferredProvider);
            AiResponse response = provider.chat(request);
            if (response.isSuccess()) {
                return response;
            }
        } catch (Exception e) {
            log.warn("首选提供商 {} 调用失败: {}", preferredProvider, e.getMessage());
        }

        // 故障转移：尝试其他可用提供商
        for (AiProvider provider : providers) {
            if (!provider.getProviderName().equalsIgnoreCase(preferredProvider) && provider.isAvailable()) {
                try {
                    log.info("故障转移到提供商: {}", provider.getProviderName());
                    AiResponse response = provider.chat(request);
                    if (response.isSuccess()) {
                        return response;
                    }
                } catch (Exception e) {
                    log.warn("故障转移提供商 {} 调用失败: {}", provider.getProviderName(), e.getMessage());
                }
            }
        }
        throw new BusinessException(503, "所有AI服务提供商均不可用");
    }

    public void setDefaultProviderName(String name) {
        this.defaultProviderName = name;
    }
}
