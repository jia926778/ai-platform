package com.example.aiplatform.service;

import com.example.aiplatform.dto.UsageStatsResponse;
import com.example.aiplatform.entity.ApiUsageLog;
import com.example.aiplatform.repository.ApiUsageLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonitorService {

    private final ApiUsageLogRepository usageLogRepository;

    public MonitorService(ApiUsageLogRepository usageLogRepository) {
        this.usageLogRepository = usageLogRepository;
    }

    public Page<ApiUsageLog> getUserUsage(Long userId, Pageable pageable) {
        return usageLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public UsageStatsResponse getUserStats(Long userId) {
        long totalCalls = usageLogRepository.countByUserId(userId);
        long totalTokens = usageLogRepository.sumTotalTokensByUserId(userId);
        List<Object[]> statsByType = usageLogRepository.getStatsByApiTypeForUser(userId);

        BigDecimal totalCost = BigDecimal.ZERO;
        List<UsageStatsResponse.ApiTypeStats> breakdown = new ArrayList<>();

        for (Object[] row : statsByType) {
            String apiType = (String) row[0];
            long calls = (Long) row[1];
            long tokens = (Long) row[2];
            BigDecimal cost = row[3] instanceof BigDecimal ? (BigDecimal) row[3] : BigDecimal.valueOf(((Number) row[3]).doubleValue());
            totalCost = totalCost.add(cost);

            breakdown.add(UsageStatsResponse.ApiTypeStats.builder()
                    .apiType(apiType)
                    .calls(calls)
                    .tokens(tokens)
                    .cost(cost)
                    .build());
        }

        return UsageStatsResponse.builder()
                .totalCalls(totalCalls)
                .totalTokens(totalTokens)
                .totalCost(totalCost)
                .breakdown(breakdown)
                .build();
    }

    public UsageStatsResponse getDashboardStats() {
        List<ApiUsageLog> allLogs = usageLogRepository.findAll();
        long totalCalls = allLogs.size();
        long totalTokens = allLogs.stream()
                .mapToLong(log -> log.getTotalTokens() != null ? log.getTotalTokens() : 0)
                .sum();
        BigDecimal totalCost = allLogs.stream()
                .map(log -> log.getCost() != null ? log.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UsageStatsResponse.builder()
                .totalCalls(totalCalls)
                .totalTokens(totalTokens)
                .totalCost(totalCost)
                .breakdown(new ArrayList<>())
                .build();
    }
}
