package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本摘要响应DTO
 * <p>
 * 封装文本摘要接口的返回结果，
 * 包括摘要内容、原文长度、摘要长度和压缩比。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {

    /** 生成的摘要文本 */
    private String summary;
    /** 原始文本长度（字符数） */
    private int originalLength;
    /** 摘要文本长度（字符数） */
    private int summaryLength;
    /** 压缩比（摘要长度/原文长度） */
    private double compressionRatio;
}
