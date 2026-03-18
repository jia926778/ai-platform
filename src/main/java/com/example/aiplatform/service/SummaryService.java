package com.example.aiplatform.service;

import com.example.aiplatform.ai.AiProvider;
import com.example.aiplatform.ai.AiProviderManager;
import com.example.aiplatform.ai.AiRequest;
import com.example.aiplatform.ai.AiResponse;
import com.example.aiplatform.dto.SummaryRequest;
import com.example.aiplatform.dto.SummaryResponse;
import com.example.aiplatform.entity.ApiUsageLog;
import com.example.aiplatform.repository.ApiUsageLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 文本摘要服务类
 * <p>
 * 调用AI模型对用户提交的长文本生成摘要。
 * 支持自定义摘要最大长度限制，并计算压缩比。
 * 每次调用都会记录API使用日志。
 * 通过AiProviderManager支持多AI提供商适配和故障转移。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Slf4j
@Service
public class SummaryService {

    /** AI服务提供商管理器 */
    private final AiProviderManager aiProviderManager;
    /** API使用日志数据访问 */
    private final ApiUsageLogRepository usageLogRepository;

    public SummaryService(AiProviderManager aiProviderManager, ApiUsageLogRepository usageLogRepository) {
        this.aiProviderManager = aiProviderManager;
        this.usageLogRepository = usageLogRepository;
    }

    /**
     * 生成文本摘要（同步）
     * <p>
     * 将原始文本发送给AI模型进行摘要生成，返回摘要内容及相关统计信息。
     * 通过AiProviderManager按提供商名称路由到对应的AI服务提供商。
     * </p>
     *
     * @param userId  当前用户ID（用于记录使用日志）
     * @param request 摘要请求参数（包含原文和可选的长度限制）
     * @return 摘要响应（包含摘要文本、原文长度、摘要长度和压缩比）
     */
    public SummaryResponse generateSummary(Long userId, SummaryRequest request) {
        // 构建摘要长度限制指令
        String maxLengthInstruction = request.getMaxLength() != null
                ? " Keep the summary under " + request.getMaxLength() + " characters."
                : " Keep the summary concise.";

        // 构建系统提示词
        String systemPrompt = "You are a professional text summarization assistant. " +
                "Provide clear, concise summaries that capture the key points." + maxLengthInstruction;

        // 构建统一AI请求
        AiRequest aiRequest = AiRequest.builder()
                .systemPrompt(systemPrompt)
                .userMessage("Please summarize the following text:\n\n" + request.getText())
                .build();

        String summaryText;
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
                summaryText = aiResponse.getContent();
                promptTokens = aiResponse.getPromptTokens();
                completionTokens = aiResponse.getCompletionTokens();
                totalTokens = aiResponse.getTotalTokens();
                modelUsed = aiResponse.getModel() != null ? aiResponse.getModel() : modelUsed;
            } else {
                status = "error";
                errorMsg = aiResponse.getErrorMessage();
                summaryText = "Failed to generate summary: " + aiResponse.getErrorMessage();
            }
        } catch (Exception e) {
            status = "error";
            errorMsg = e.getMessage();
            summaryText = "Failed to generate summary: " + e.getMessage();
        }

        // 记录API调用日志
        ApiUsageLog usageLog = ApiUsageLog.builder()
                .userId(userId)
                .apiType("summary")
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

        // 计算摘要压缩比
        int originalLength = request.getText().length();
        int summaryLength = summaryText.length();
        double compressionRatio = originalLength > 0
                ? (double) summaryLength / originalLength
                : 0.0;

        return SummaryResponse.builder()
                .summary(summaryText)
                .originalLength(originalLength)
                .summaryLength(summaryLength)
                .compressionRatio(Math.round(compressionRatio * 100.0) / 100.0)
                .build();
    }

    /**
     * 异步生成文本摘要
     * <p>
     * 使用AI提供商的异步接口调用，适用于不需要立即返回结果的场景。
     * </p>
     *
     * @param userId  当前用户ID
     * @param request 摘要请求参数
     * @return CompletableFuture包装的摘要响应
     */
    public CompletableFuture<SummaryResponse> generateSummaryAsync(Long userId, SummaryRequest request) {
        return CompletableFuture.supplyAsync(() -> generateSummary(userId, request));
    }
}
