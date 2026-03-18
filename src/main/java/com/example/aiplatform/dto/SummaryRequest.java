package com.example.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本摘要请求DTO
 * <p>
 * 封装文本摘要接口的请求参数，
 * 包括待摘要的原始文本和可选的最大摘要长度限制。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryRequest {

    /** 待摘要的原始文本（必填） */
    @NotBlank(message = "Text is required")
    private String text;

    /** 摘要最大长度限制（可选） */
    private Integer maxLength;
}
