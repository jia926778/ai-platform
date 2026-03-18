package com.example.aiplatform.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI统一响应结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {
    /** 响应内容 */
    private String content;
    /** 使用的模型 */
    private String model;
    /** 提供商名称 */
    private String provider;
    /** 提示词Token数 */
    @Builder.Default
    private Integer promptTokens = 0;
    /** 生成内容Token数 */
    @Builder.Default
    private Integer completionTokens = 0;
    /** 总Token数 */
    @Builder.Default
    private Integer totalTokens = 0;
    /** 是否成功 */
    @Builder.Default
    private boolean success = true;
    /** 错误信息 */
    private String errorMessage;
}
