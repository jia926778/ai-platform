package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageStatsResponse {

    private long totalCalls;
    private long totalTokens;
    private BigDecimal totalCost;
    private List<ApiTypeStats> breakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiTypeStats {
        private String apiType;
        private long calls;
        private long tokens;
        private BigDecimal cost;
    }
}
