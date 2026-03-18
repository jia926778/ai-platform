package com.example.aiplatform.service;

import com.example.aiplatform.ai.AiProvider;
import com.example.aiplatform.ai.AiProviderManager;
import com.example.aiplatform.ai.AiRequest;
import com.example.aiplatform.ai.AiResponse;
import com.example.aiplatform.dto.CodeGenRequest;
import com.example.aiplatform.dto.CodeGenResponse;
import com.example.aiplatform.entity.ApiUsageLog;
import com.example.aiplatform.repository.ApiUsageLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 代码生成服务类
 * <p>
 * 调用AI模型根据用户的自然语言描述自动生成代码。
 * 支持指定编程语言和框架，生成生产级别的代码。
 * 每次调用都会记录API使用日志。
 * 通过AiProviderManager支持多AI提供商适配和故障转移。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Slf4j
@Service
public class CodeGenService {

    /** AI服务提供商管理器 */
    private final AiProviderManager aiProviderManager;
    /** API使用日志数据访问 */
    private final ApiUsageLogRepository usageLogRepository;

    public CodeGenService(AiProviderManager aiProviderManager, ApiUsageLogRepository usageLogRepository) {
        this.aiProviderManager = aiProviderManager;
        this.usageLogRepository = usageLogRepository;
    }

    /**
     * 根据功能描述生成代码（同步）
     * <p>
     * 将用户的功能需求描述发送给AI模型，生成指定语言和框架的代码。
     * 支持自定义编程语言（默认Java）和可选的框架指定。
     * 通过AiProviderManager按提供商名称路由到对应的AI服务提供商。
     * </p>
     *
     * @param userId  当前用户ID（用于记录使用日志）
     * @param request 代码生成请求参数
     * @return 代码生成响应（包含生成的代码、语言、描述和token消耗）
     */
    public CodeGenResponse generateCode(Long userId, CodeGenRequest request) {
        // 确定目标编程语言，默认Java
        String language = request.getLanguage() != null ? request.getLanguage() : "Java";
        // 如果指定了框架，构建框架描述文本
        String frameworkContext = request.getFramework() != null && !request.getFramework().isEmpty()
                ? " using the " + request.getFramework() + " framework"
                : "";

        // 构建系统提示词，指定AI作为专业程序员角色
        String systemPrompt = "You are an expert " + language + " programmer. " +
                "Generate clean, well-documented, production-ready code. " +
                "Only output the code without additional explanation unless necessary for understanding.";

        // 构建用户消息，包含需求描述和语言/框架要求
        String userMessage = "Generate " + language + " code" + frameworkContext +
                " for the following requirement:\n\n" + request.getDescription();

        // 构建统一AI请求
        AiRequest aiRequest = AiRequest.builder()
                .systemPrompt(systemPrompt)
                .userMessage(userMessage)
                .build();

        String generatedCode;
        int totalTokens = 0;
        int promptTokens = 0;
        int completionTokens = 0;
        String status = "success";
        String errorMsg = null;
        String modelUsed = "gpt-3.5-turbo";

        try {
            // 通过AiProviderManager路由到对应提供商并调用
            AiProvider provider;
            if (request.getProvider() != null && !request.getProvider().isEmpty()) {
                provider = aiProviderManager.getProvider(request.getProvider());
            } else {
                provider = aiProviderManager.getDefaultProvider();
            }

            AiResponse aiResponse = provider.chat(aiRequest);

            if (aiResponse.isSuccess()) {
                generatedCode = aiResponse.getContent();
                promptTokens = aiResponse.getPromptTokens();
                completionTokens = aiResponse.getCompletionTokens();
                totalTokens = aiResponse.getTotalTokens();
                modelUsed = aiResponse.getModel() != null ? aiResponse.getModel() : modelUsed;
            } else {
                status = "error";
                errorMsg = aiResponse.getErrorMessage();
                generatedCode = "// Failed to generate code: " + aiResponse.getErrorMessage();
            }
        } catch (Exception e) {
            status = "error";
            errorMsg = e.getMessage();
            generatedCode = "// Failed to generate code: " + e.getMessage();
        }

        // 记录API调用日志
        ApiUsageLog usageLog = ApiUsageLog.builder()
                .userId(userId)
                .apiType("codegen")
                .model(modelUsed)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .cost(BigDecimal.valueOf(totalTokens * 0.000002))
                .status(status)
                .errorMsg(errorMsg)
                .createdAt(LocalDateTime.now())
                .build();
        usageLogRepository.save(usageLog);

        return CodeGenResponse.builder()
                .code(generatedCode)
                .language(language)
                .description(request.getDescription())
                .tokens(totalTokens)
                .build();
    }

    /**
     * 异步生成代码
     * <p>
     * 使用AI提供商的异步接口调用，适用于不需要立即返回结果的场景。
     * </p>
     *
     * @param userId  当前用户ID
     * @param request 代码生成请求参数
     * @return CompletableFuture包装的代码生成响应
     */
    public CompletableFuture<CodeGenResponse> generateCodeAsync(Long userId, CodeGenRequest request) {
        return CompletableFuture.supplyAsync(() -> generateCode(userId, request));
    }
}
