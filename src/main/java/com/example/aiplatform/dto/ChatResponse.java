package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应DTO
 * <p>
 * 封装AI聊天接口返回的响应数据，
 * 包括会话ID、AI回复消息、消息角色、token消耗和会话标题。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /** 所属会话ID */
    private Long conversationId;
    /** AI回复的消息内容 */
    private String message;
    /** 消息角色（通常为 assistant） */
    private String role;
    /** 本次回复消耗的token数量 */
    private Integer tokens;
    /** 会话标题 */
    private String conversationTitle;
}
