package com.example.aiplatform.controller;

import com.example.aiplatform.ai.AiProviderManager;
import com.example.aiplatform.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AI服务提供商控制器
 * <p>查询当前系统支持的AI服务提供商及其状态</p>
 */
@RestController
@RequestMapping("/api/ai-providers")
@RequiredArgsConstructor
public class AiProviderController {

    private final AiProviderManager providerManager;

    /**
     * 获取所有AI服务提供商信息
     */
    @GetMapping
    public ApiResult<List<Map<String, Object>>> listProviders() {
        return ApiResult.success(providerManager.listProviders());
    }
}
