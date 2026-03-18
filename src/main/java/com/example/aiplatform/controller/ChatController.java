package com.example.aiplatform.controller;

import com.example.aiplatform.dto.ApiResult;
import com.example.aiplatform.dto.ChatRequest;
import com.example.aiplatform.dto.ChatResponse;
import com.example.aiplatform.entity.ChatConversation;
import com.example.aiplatform.entity.ChatMessage;
import com.example.aiplatform.repository.SysUserRepository;
import com.example.aiplatform.service.ChatService;
import com.example.aiplatform.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final SysUserRepository userRepository;

    public ChatController(ChatService chatService, SysUserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send")
    public ApiResult<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        ChatResponse response = chatService.sendMessage(userId, request);
        return ApiResult.success(response);
    }

    @GetMapping("/conversations")
    public ApiResult<List<ChatConversation>> getConversations() {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        List<ChatConversation> conversations = chatService.getConversations(userId);
        return ApiResult.success(conversations);
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResult<List<ChatMessage>> getMessages(@PathVariable Long id) {
        List<ChatMessage> messages = chatService.getMessages(id);
        return ApiResult.success(messages);
    }

    @DeleteMapping("/conversations/{id}")
    public ApiResult<Void> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return ApiResult.success("Conversation deleted", null);
    }
}
