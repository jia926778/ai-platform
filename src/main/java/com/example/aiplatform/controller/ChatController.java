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

/**
 * 聊天控制器
 * <p>
 * 提供AI聊天相关的REST API接口，包括发送消息、获取会话列表、
 * 获取会话消息历史和删除会话。所有接口需要JWT认证。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final SysUserRepository userRepository;

    public ChatController(ChatService chatService, SysUserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    /**
     * 发送聊天消息并获取AI回复
     *
     * @param request 聊天请求参数（会话ID、消息内容、模型选择）
     * @return AI回复的聊天响应
     */
    @PostMapping("/send")
    public ApiResult<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        ChatResponse response = chatService.sendMessage(userId, request);
        return ApiResult.success(response);
    }

    /**
     * 获取当前用户的所有聊天会话列表
     *
     * @return 会话列表
     */
    @GetMapping("/conversations")
    public ApiResult<List<ChatConversation>> getConversations() {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        List<ChatConversation> conversations = chatService.getConversations(userId);
        return ApiResult.success(conversations);
    }

    /**
     * 获取指定会话的消息历史
     *
     * @param id 会话ID
     * @return 消息列表
     */
    @GetMapping("/conversations/{id}/messages")
    public ApiResult<List<ChatMessage>> getMessages(@PathVariable Long id) {
        List<ChatMessage> messages = chatService.getMessages(id);
        return ApiResult.success(messages);
    }

    /**
     * 删除指定会话及其所有消息
     *
     * @param id 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/conversations/{id}")
    public ApiResult<Void> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return ApiResult.success("Conversation deleted", null);
    }
}
