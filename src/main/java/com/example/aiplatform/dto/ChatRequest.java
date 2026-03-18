package com.example.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求DTO
 * <p>
 * 封装用户发送聊天消息时的请求参数，
 * 包括会话ID（可选，不传则创建新会话）、消息内容和AI模型选择。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /** 会话ID（为空时创建新会话） */
    private Long conversationId;

    /** 用户发送的消息内容（必填） */
    @NotBlank(message = "Message is required")
    private String message;

    /** 指定使用的AI模型（可选） */
    private String model;

    /** AI服务提供商（可选，默认使用系统配置） */
    private String provider;
}
