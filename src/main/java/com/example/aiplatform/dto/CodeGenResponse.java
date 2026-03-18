package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码生成响应DTO
 * <p>
 * 封装代码生成接口的返回结果，
 * 包括生成的代码内容、使用的编程语言、功能描述和token消耗。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenResponse {

    /** 生成的代码内容 */
    private String code;
    /** 代码所使用的编程语言 */
    private String language;
    /** 代码功能描述 */
    private String description;
    /** 本次生成消耗的token数量 */
    private Integer tokens;
}
