package com.example.aiplatform.service;

import com.example.aiplatform.ai.AiMessage;
import com.example.aiplatform.ai.AiProvider;
import com.example.aiplatform.ai.AiProviderManager;
import com.example.aiplatform.ai.AiRequest;
import com.example.aiplatform.ai.AiResponse;
import com.example.aiplatform.dto.ChatRequest;
import com.example.aiplatform.dto.ChatResponse;
import com.example.aiplatform.entity.ApiUsageLog;
import com.example.aiplatform.entity.ChatConversation;
import com.example.aiplatform.entity.ChatMessage;
import com.example.aiplatform.exception.BusinessException;
import com.example.aiplatform.repository.ApiUsageLogRepository;
import com.example.aiplatform.repository.ChatConversationRepository;
import com.example.aiplatform.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 聊天服务类
 * <p>
 * 提供AI聊天对话功能，包括发送消息、获取会话列表、获取消息历史和删除会话。
 * 每次聊天调用都会记录到API使用日志中，用于用量统计和费用核算。
 * 支持多轮对话，通过会话历史构建上下文传递给AI模型。
 * 通过AiProviderManager支持多AI提供商适配和故障转移。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Slf4j
@Service
public class ChatService {

    /** AI服务提供商管理器 */
    private final AiProviderManager aiProviderManager;
    /** 会话数据访问 */
    private final ChatConversationRepository conversationRepository;
    /** 消息数据访问 */
    private final ChatMessageRepository messageRepository;
    /** API使用日志数据访问 */
    private final ApiUsageLogRepository usageLogRepository;

    public ChatService(AiProviderManager aiProviderManager,
                       ChatConversationRepository conversationRepository,
                       ChatMessageRepository messageRepository,
                       ApiUsageLogRepository usageLogRepository) {
        this.aiProviderManager = aiProviderManager;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.usageLogRepository = usageLogRepository;
    }

    /**
     * 发送聊天消息并获取AI回复（同步）
     * <p>
     * 如果指定了会话ID则在已有会话中继续对话，否则创建新会话。
     * 将用户消息保存后，构建完整对话历史作为上下文调用AI模型，
     * 最后保存AI回复并记录API使用日志。
     * 通过AiProviderManager按模型或提供商名称路由到对应的AI服务提供商。
     * </p>
     *
     * @param userId  当前用户ID
     * @param request 聊天请求参数
     * @return AI回复的聊天响应
     * @throws BusinessException 会话不存在或无权访问时抛出
     */
    @Transactional
    public ChatResponse sendMessage(Long userId, ChatRequest request) {
        ChatConversation conversation;
        if (request.getConversationId() != null) {
            // 查找已有会话并验证所有权
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new BusinessException("Conversation not found"));
            if (!conversation.getUserId().equals(userId)) {
                throw new BusinessException(403, "Access denied to this conversation");
            }
        } else {
            // 创建新会话，以消息前50个字符作为标题
            conversation = ChatConversation.builder()
                    .userId(userId)
                    .title(request.getMessage().length() > 50
                            ? request.getMessage().substring(0, 50) + "..."
                            : request.getMessage())
                    .model(request.getModel() != null ? request.getModel() : "gpt-3.5-turbo")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            conversation = conversationRepository.save(conversation);
        }

        // 保存用户消息到数据库
        ChatMessage userMessage = ChatMessage.builder()
                .conversationId(conversation.getId())
                .role("user")
                .content(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(userMessage);

        // 构建对话历史上下文（转换为统一AiMessage格式）
        List<ChatMessage> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        List<AiMessage> aiMessages = new ArrayList<>();
        for (ChatMessage msg : history) {
            aiMessages.add(AiMessage.builder()
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .build());
        }

        // 构建统一AI请求
        AiRequest aiRequest = AiRequest.builder()
                .systemPrompt("You are a helpful AI assistant.")
                .messages(aiMessages)
                .model(request.getModel())
                .build();

        // 通过AiProviderManager路由到对应提供商并调用
        String assistantContent;
        Integer totalTokens = 0;
        Integer promptTokens = 0;
        Integer completionTokens = 0;
        String status = "success";
        String errorMsg = null;

        try {
            AiProvider provider;
            if (request.getProvider() != null && !request.getProvider().isEmpty()) {
                // 按指定提供商名称获取
                provider = aiProviderManager.getProvider(request.getProvider());
            } else {
                // 按模型名称自动匹配提供商
                provider = aiProviderManager.getProviderByModel(request.getModel());
            }

            AiResponse aiResponse = provider.chat(aiRequest);

            if (aiResponse.isSuccess()) {
                assistantContent = aiResponse.getContent();
                promptTokens = aiResponse.getPromptTokens();
                completionTokens = aiResponse.getCompletionTokens();
                totalTokens = aiResponse.getTotalTokens();
            } else {
                // AI提供商返回失败响应
                status = "error";
                errorMsg = aiResponse.getErrorMessage();
                assistantContent = "Sorry, an error occurred while processing your request: " + aiResponse.getErrorMessage();
            }
        } catch (Exception e) {
            // AI调用失败时记录错误并返回错误提示
            status = "error";
            errorMsg = e.getMessage();
            assistantContent = "Sorry, an error occurred while processing your request: " + e.getMessage();
            totalTokens = 0;
        }

        // 保存AI回复消息到数据库
        ChatMessage assistantMessage = ChatMessage.builder()
                .conversationId(conversation.getId())
                .role("assistant")
                .content(assistantContent)
                .tokens(totalTokens)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(assistantMessage);

        // 更新会话的最后活动时间
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // 记录API调用日志（用于用量统计和费用核算）
        ApiUsageLog usageLog = ApiUsageLog.builder()
                .userId(userId)
                .apiType("chat")
                .model(conversation.getModel())
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .cost(BigDecimal.valueOf(totalTokens * 0.000002))
                .status(status)
                .errorMsg(errorMsg)
                .createdAt(LocalDateTime.now())
                .build();
        usageLogRepository.save(usageLog);

        return ChatResponse.builder()
                .conversationId(conversation.getId())
                .message(assistantContent)
                .role("assistant")
                .tokens(totalTokens)
                .conversationTitle(conversation.getTitle())
                .build();
    }

    /**
     * 异步发送聊天消息并获取AI回复
     * <p>
     * 使用AI提供商的异步接口调用，适用于不需要立即返回结果的场景。
     * </p>
     *
     * @param userId  当前用户ID
     * @param request 聊天请求参数
     * @return CompletableFuture包装的聊天响应
     */
    public CompletableFuture<ChatResponse> sendMessageAsync(Long userId, ChatRequest request) {
        return CompletableFuture.supplyAsync(() -> sendMessage(userId, request));
    }

    /**
     * 获取指定用户的所有聊天会话列表
     *
     * @param userId 用户ID
     * @return 会话列表（按最后更新时间降序排列）
     */
    public List<ChatConversation> getConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    /**
     * 获取指定会话的所有消息
     *
     * @param conversationId 会话ID
     * @return 消息列表（按创建时间升序排列）
     */
    public List<ChatMessage> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * 删除指定会话及其所有消息
     *
     * @param id 会话ID
     * @throws BusinessException 会话不存在时抛出
     */
    @Transactional
    public void deleteConversation(Long id) {
        if (!conversationRepository.existsById(id)) {
            throw new BusinessException("Conversation not found");
        }
        // 先删除会话下的所有消息，再删除会话本身
        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);
        messageRepository.deleteAll(messages);
        conversationRepository.deleteById(id);
    }
}
