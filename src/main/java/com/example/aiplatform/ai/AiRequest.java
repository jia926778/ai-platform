package com.example.aiplatform.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * AI统一请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {
    /** 系统提示词 */
    private String systemPrompt;
    /** 消息历史 */
    private List<AiMessage> messages;
    /** 用户当前输入 */
    private String userMessage;
    /** 指定模型（可选） */
    private String model;
    /** 温度参数 */
    @Builder.Default
    private Double temperature = 0.7;
    /** 最大Token数 */
    @Builder.Default
    private Integer maxTokens = 2048;
}
