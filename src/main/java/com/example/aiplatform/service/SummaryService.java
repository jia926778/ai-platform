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

@Service
public class SummaryService {

    private final ChatModel chatModel;
    private final ApiUsageLogRepository usageLogRepository;

    public SummaryService(ChatModel chatModel, ApiUsageLogRepository usageLogRepository) {
        this.chatModel = chatModel;
        this.usageLogRepository = usageLogRepository;
    }

    public SummaryResponse generateSummary(Long userId, SummaryRequest request) {
        String maxLengthInstruction = request.getMaxLength() != null
                ? " Keep the summary under " + request.getMaxLength() + " characters."
                : " Keep the summary concise.";

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
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            summaryText = aiResponse.getResult().getOutput().getContent();

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

        // Log usage
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
