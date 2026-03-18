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

/**
 * 监控服务类
 * <p>
 * 提供API使用量的监控和统计功能。
 * 支持查询用户个人的使用日志、按API类型分组的统计数据，
 * 以及管理员查看的全局仪表盘数据。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Service
public class MonitorService {

    private final ApiUsageLogRepository usageLogRepository;

    public MonitorService(ApiUsageLogRepository usageLogRepository) {
        this.usageLogRepository = usageLogRepository;
    }

    /**
     * 分页查询指定用户的API使用日志
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 分页的使用日志数据
     */
    public Page<ApiUsageLog> getUserUsage(Long userId, Pageable pageable) {
        return usageLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取指定用户的API使用量统计数据
     * <p>
     * 包含总调用次数、总token消耗量和按API类型分类的明细统计。
     * </p>
     *
     * @param userId 用户ID
     * @return 使用量统计响应
     */
    public UsageStatsResponse getUserStats(Long userId) {
        long totalCalls = usageLogRepository.countByUserId(userId);
        long totalTokens = usageLogRepository.sumTotalTokensByUserId(userId);
        List<Object[]> statsByType = usageLogRepository.getStatsByApiTypeForUser(userId);

        BigDecimal totalCost = BigDecimal.ZERO;
        List<UsageStatsResponse.ApiTypeStats> breakdown = new ArrayList<>();

        // 遍历按API类型分组的统计结果，构建明细列表
        for (Object[] row : statsByType) {
            String apiType = (String) row[0];
            long calls = (Long) row[1];
            long tokens = (Long) row[2];
            // 兼容不同数据库返回的数值类型
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

    /**
     * 获取管理员仪表盘的全局统计数据
     * <p>
     * 汇总所有用户的API调用次数、总token消耗量和总费用。
     * </p>
     *
     * @return 全局使用量统计响应
     */
    public UsageStatsResponse getDashboardStats() {
        // 查询所有API使用日志
        List<ApiUsageLog> allLogs = usageLogRepository.findAll();
        long totalCalls = allLogs.size();
        // 计算总token消耗量（空值按0处理）
        long totalTokens = allLogs.stream()
                .mapToLong(log -> log.getTotalTokens() != null ? log.getTotalTokens() : 0)
                .sum();
        // 计算总费用（空值按0处理）
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
