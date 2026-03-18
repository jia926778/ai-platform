package com.example.aiplatform.ai.provider;

import com.example.aiplatform.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * OpenAI服务提供商实现
 * <p>
 * 基于Spring AI框架调用OpenAI API，支持自动重试和异步调用。
 * </p>
 */
@Slf4j
@Component
public class OpenAiProvider implements AiProvider {

    @Autowired
    private ChatModel chatModel;

    @Override
    public String getProviderName() {
        return "OPENAI";
    }

    /**
     * 同步调用OpenAI（带重试机制）
     * <p>
     * 对临时网络异常自动重试，最多3次，采用指数退避策略。
     * </p>
     */
    @Override
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public AiResponse chat(AiRequest request) {
        try {
            List<Message> messages = buildMessages(request);
            Prompt prompt = new Prompt(messages);
            ChatResponse response = chatModel.call(prompt);

            String content = response.getResult().getOutput().getContent();
            var usage = response.getMetadata().getUsage();

            return AiResponse.builder()
                    .content(content)
                    .model(request.getModel() != null ? request.getModel() : "gpt-3.5-turbo")
                    .provider(getProviderName())
                    .promptTokens((int) usage.getPromptTokens())
                    .completionTokens((int) usage.getGenerationTokens())
                    .totalTokens((int) usage.getTotalTokens())
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("OpenAI调用失败: {}", e.getMessage(), e);
            throw e; // 抛出异常触发重试
        }
    }

    /**
     * 异步调用OpenAI
     */
    @Override
    @Async("aiTaskExecutor")
    public CompletableFuture<AiResponse> chatAsync(AiRequest request) {
        try {
            AiResponse response = chat(request);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            AiResponse errorResponse = AiResponse.builder()
                    .success(false)
                    .provider(getProviderName())
                    .errorMessage(e.getMessage())
                    .build();
            return CompletableFuture.completedFuture(errorResponse);
        }
    }

    @Override
    public boolean isAvailable() {
        return chatModel != null;
    }

    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList("gpt-3.5-turbo", "gpt-4", "gpt-4-turbo", "gpt-4o");
    }

    /**
     * 将统一请求格式转换为Spring AI的Message列表
     */
    private List<Message> buildMessages(AiRequest request) {
        List<Message> messages = new ArrayList<>();

        // 添加系统提示词
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(new SystemMessage(request.getSystemPrompt()));
        }

        // 添加历史消息
        if (request.getMessages() != null) {
            for (AiMessage msg : request.getMessages()) {
                switch (msg.getRole()) {
                    case "user" -> messages.add(new UserMessage(msg.getContent()));
                    case "assistant" -> messages.add(new AssistantMessage(msg.getContent()));
                    case "system" -> messages.add(new SystemMessage(msg.getContent()));
                }
            }
        }

        // 添加当前用户消息
        if (request.getUserMessage() != null && !request.getUserMessage().isEmpty()) {
            messages.add(new UserMessage(request.getUserMessage()));
        }

        return messages;
    }
}
