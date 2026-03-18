package com.example.aiplatform.service;

import com.example.aiplatform.dto.ChatRequest;
import com.example.aiplatform.dto.ChatResponse;
import com.example.aiplatform.entity.ApiUsageLog;
import com.example.aiplatform.entity.ChatConversation;
import com.example.aiplatform.entity.ChatMessage;
import com.example.aiplatform.exception.BusinessException;
import com.example.aiplatform.repository.ApiUsageLogRepository;
import com.example.aiplatform.repository.ChatConversationRepository;
import com.example.aiplatform.repository.ChatMessageRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ChatModel chatModel;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ApiUsageLogRepository usageLogRepository;

    public ChatService(ChatModel chatModel,
                       ChatConversationRepository conversationRepository,
                       ChatMessageRepository messageRepository,
                       ApiUsageLogRepository usageLogRepository) {
        this.chatModel = chatModel;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.usageLogRepository = usageLogRepository;
    }

    @Transactional
    public ChatResponse sendMessage(Long userId, ChatRequest request) {
        ChatConversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new BusinessException("Conversation not found"));
            if (!conversation.getUserId().equals(userId)) {
                throw new BusinessException(403, "Access denied to this conversation");
            }
        } else {
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

        // Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .conversationId(conversation.getId())
                .role("user")
                .content(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(userMessage);

        // Build conversation history for context
        List<ChatMessage> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("You are a helpful AI assistant."));
        for (ChatMessage msg : history) {
            switch (msg.getRole()) {
                case "user" -> messages.add(new UserMessage(msg.getContent()));
                case "assistant" -> messages.add(new AssistantMessage(msg.getContent()));
                case "system" -> messages.add(new SystemMessage(msg.getContent()));
            }
        }

        // Call AI
        String assistantContent;
        Integer totalTokens = 0;
        Integer promptTokens = 0;
        Integer completionTokens = 0;
        String status = "success";
        String errorMsg = null;

        try {
            Prompt prompt = new Prompt(messages);
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            assistantContent = aiResponse.getResult().getOutput().getContent();

            if (aiResponse.getMetadata() != null && aiResponse.getMetadata().getUsage() != null) {
                promptTokens = (int) aiResponse.getMetadata().getUsage().getPromptTokens();
                completionTokens = (int) aiResponse.getMetadata().getUsage().getGenerationTokens();
                totalTokens = promptTokens + completionTokens;
            }
        } catch (Exception e) {
            status = "error";
            errorMsg = e.getMessage();
            assistantContent = "Sorry, an error occurred while processing your request: " + e.getMessage();
            totalTokens = 0;
        }

        // Save assistant message
        ChatMessage assistantMessage = ChatMessage.builder()
                .conversationId(conversation.getId())
                .role("assistant")
                .content(assistantContent)
                .tokens(totalTokens)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(assistantMessage);

        // Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Log API usage
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

    public List<ChatConversation> getConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public List<ChatMessage> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public void deleteConversation(Long id) {
        if (!conversationRepository.existsById(id)) {
            throw new BusinessException("Conversation not found");
        }
        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);
        messageRepository.deleteAll(messages);
        conversationRepository.deleteById(id);
    }
}
