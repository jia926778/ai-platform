package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 高频问题统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentQuestionDTO {
    /** 问题关键词 */
    private String keyword;
    /** 出现次数 */
    private Long count;
}
