package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * API使用量统计响应DTO
 * <p>
 * 封装用户API使用量的统计结果，包括总调用次数、总token数、
 * 总费用以及按API类型的分类统计明细。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageStatsResponse {

    /** 总API调用次数 */
    private long totalCalls;
    /** 总token消耗数量 */
    private long totalTokens;
    /** 总费用 */
    private BigDecimal totalCost;
    /** 按API类型的分类统计明细 */
    private List<ApiTypeStats> breakdown;

    /**
     * 按API类型的统计数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiTypeStats {
        /** API类型名称 */
        private String apiType;
        /** 该类型的调用次数 */
        private long calls;
        /** 该类型消耗的token数量 */
        private long tokens;
        /** 该类型的总费用 */
        private BigDecimal cost;
    }
}
