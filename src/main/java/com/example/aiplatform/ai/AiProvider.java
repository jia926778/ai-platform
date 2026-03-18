package com.example.aiplatform.ai;

/**
 * AI服务提供商接口
 * <p>
 * 定义AI服务的统一调用规范，所有AI提供商（OpenAI、Claude、通义千问等）
 * 均需实现此接口，实现多平台适配。
 * </p>
 */
public interface AiProvider {

    /**
     * 获取提供商名称
     * @return 提供商标识（如 OPENAI, CLAUDE, QIANWEN）
     */
    String getProviderName();

    /**
     * 同步调用AI对话
     * @param request AI请求参数
     * @return AI响应结果
     */
    AiResponse chat(AiRequest request);

    /**
     * 异步调用AI对话
     * @param request AI请求参数
     * @return CompletableFuture包装的AI响应
     */
    java.util.concurrent.CompletableFuture<AiResponse> chatAsync(AiRequest request);

    /**
     * 检查提供商是否可用
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取支持的模型列表
     * @return 模型名称列表
     */
    java.util.List<String> getSupportedModels();
}
