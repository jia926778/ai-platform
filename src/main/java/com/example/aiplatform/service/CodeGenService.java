package com.example.aiplatform.service;

import com.example.aiplatform.dto.CodeGenRequest;
import com.example.aiplatform.dto.CodeGenResponse;
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
public class CodeGenService {

    private final ChatModel chatModel;
    private final ApiUsageLogRepository usageLogRepository;

    public CodeGenService(ChatModel chatModel, ApiUsageLogRepository usageLogRepository) {
        this.chatModel = chatModel;
        this.usageLogRepository = usageLogRepository;
    }

    public CodeGenResponse generateCode(Long userId, CodeGenRequest request) {
        String language = request.getLanguage() != null ? request.getLanguage() : "Java";
        String frameworkContext = request.getFramework() != null && !request.getFramework().isEmpty()
                ? " using the " + request.getFramework() + " framework"
                : "";

        SystemMessage systemMessage = new SystemMessage(
                "You are an expert " + language + " programmer. " +
                "Generate clean, well-documented, production-ready code. " +
                "Only output the code without additional explanation unless necessary for understanding.");

        UserMessage userMessage = new UserMessage(
                "Generate " + language + " code" + frameworkContext +
                " for the following requirement:\n\n" + request.getDescription());

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        String generatedCode;
        int totalTokens = 0;
        int promptTokens = 0;
        int completionTokens = 0;
        String status = "success";
        String errorMsg = null;

        try {
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            generatedCode = aiResponse.getResult().getOutput().getContent();

            if (aiResponse.getMetadata() != null && aiResponse.getMetadata().getUsage() != null) {
                promptTokens = (int) aiResponse.getMetadata().getUsage().getPromptTokens();
                completionTokens = (int) aiResponse.getMetadata().getUsage().getGenerationTokens();
                totalTokens = promptTokens + completionTokens;
            }
        } catch (Exception e) {
            status = "error";
            errorMsg = e.getMessage();
            generatedCode = "// Failed to generate code: " + e.getMessage();
        }

        // Log usage
        ApiUsageLog usageLog = ApiUsageLog.builder()
                .userId(userId)
                .apiType("codegen")
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

        return CodeGenResponse.builder()
                .code(generatedCode)
                .language(language)
                .description(request.getDescription())
                .tokens(totalTokens)
                .build();
    }
}
