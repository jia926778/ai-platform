package com.example.aiplatform.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI消息对象（统一消息格式）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {
    /** 角色：system / user / assistant */
    private String role;
    /** 消息内容 */
    private String content;
}
