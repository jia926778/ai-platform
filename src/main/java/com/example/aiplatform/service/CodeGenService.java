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

/**
 * 代码生成服务类
 * <p>
 * 调用AI模型根据用户的自然语言描述自动生成代码。
 * 支持指定编程语言和框架，生成生产级别的代码。
 * 每次调用都会记录API使用日志。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Service
public class CodeGenService {

    /** Spring AI聊天模型 */
    private final ChatModel chatModel;
    /** API使用日志数据访问 */
    private final ApiUsageLogRepository usageLogRepository;

    public CodeGenService(ChatModel chatModel, ApiUsageLogRepository usageLogRepository) {
        this.chatModel = chatModel;
        this.usageLogRepository = usageLogRepository;
    }

    /**
     * 根据功能描述生成代码
     * <p>
     * 将用户的功能需求描述发送给AI模型，生成指定语言和框架的代码。
     * 支持自定义编程语言（默认Java）和可选的框架指定。
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
        SystemMessage systemMessage = new SystemMessage(
                "You are an expert " + language + " programmer. " +
                "Generate clean, well-documented, production-ready code. " +
                "Only output the code without additional explanation unless necessary for understanding.");

        // 构建用户消息，包含需求描述和语言/框架要求
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
            // 调用AI模型生成代码
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            generatedCode = aiResponse.getResult().getOutput().getContent();

            // 提取token用量信息
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

        // 记录API调用日志
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
