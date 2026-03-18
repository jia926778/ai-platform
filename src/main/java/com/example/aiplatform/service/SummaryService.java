package com.example.aiplatform.service;

import com.example.aiplatform.dto.SummaryRequest;
import com.example.aiplatform.dto.SummaryResponse;
import com.example.aiplatform.entity.ApiUsageLog;
import com.example.aiplatform.repository.ApiUsageLogRepository;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文本摘要服务类
 * <p>
 * 调用AI模型对用户提交的长文本生成摘要。
 * 支持自定义摘要最大长度限制，并计算压缩比。
 * 每次调用都会记录API使用日志。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Service
public class SummaryService {

    /** Spring AI聊天模型 */
    private final ChatModel chatModel;
    /** API使用日志数据访问 */
    private final ApiUsageLogRepository usageLogRepository;

    public SummaryService(ChatModel chatModel, ApiUsageLogRepository usageLogRepository) {
        this.chatModel = chatModel;
        this.usageLogRepository = usageLogRepository;
    }

    /**
     * 生成文本摘要
     * <p>
     * 将原始文本发送给AI模型进行摘要生成，返回摘要内容及相关统计信息。
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

        // 构建系统提示词和用户消息
        SystemMessage systemMessage = new SystemMessage(
                "You are a professional text summarization assistant. " +
                "Provide clear, concise summaries that capture the key points." + maxLengthInstruction);
        UserMessage userMessage = new UserMessage(
                "Please summarize the following text:\n\n" + request.getText());

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        String summaryText;
        int totalTokens = 0;
        int promptTokens = 0;
        int completionTokens = 0;
        String status = "success";
        String errorMsg = null;

        try {
            // 调用AI模型生成摘要
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            summaryText = aiResponse.getResult().getOutput().getContent();

            // 提取token用量信息
            if (aiResponse.getMetadata() != null && aiResponse.getMetadata().getUsage() != null) {
                promptTokens = (int) aiResponse.getMetadata().getUsage().getPromptTokens();
                completionTokens = (int) aiResponse.getMetadata().getUsage().getGenerationTokens();
                totalTokens = promptTokens + completionTokens;
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
                .model("gpt-3.5-turbo")
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
}
