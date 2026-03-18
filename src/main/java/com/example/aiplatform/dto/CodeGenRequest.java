package com.example.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码生成请求DTO
 * <p>
 * 封装代码生成接口的请求参数，
 * 包括功能描述、目标编程语言和可选的框架指定。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenRequest {

    /** 代码功能需求描述（必填） */
    @NotBlank(message = "Description is required")
    private String description;

    /** 目标编程语言，默认为 Java */
    @Builder.Default
    private String language = "Java";

    /** 使用的框架（可选，如：Spring Boot、React等） */
    private String framework;
}
