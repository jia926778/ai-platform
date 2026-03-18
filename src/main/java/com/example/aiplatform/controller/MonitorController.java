package com.example.aiplatform.controller;

import com.example.aiplatform.dto.ApiResult;
import com.example.aiplatform.dto.UsageStatsResponse;
import com.example.aiplatform.entity.ApiUsageLog;
import com.example.aiplatform.repository.SysUserRepository;
import com.example.aiplatform.service.MonitorService;
import com.example.aiplatform.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监控控制器
 * <p>
 * 提供API使用量监控相关的REST API接口。
 * 普通用户可查看个人使用日志和统计数据，管理员可查看全局仪表盘。
 * 需要JWT认证，仪表盘接口需要ADMIN角色。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorService monitorService;
    private final SysUserRepository userRepository;

    public MonitorController(MonitorService monitorService, SysUserRepository userRepository) {
        this.monitorService = monitorService;
        this.userRepository = userRepository;
    }

    /**
     * 分页查询当前用户的API使用日志
     *
     * @param page 页码（从0开始，默认0）
     * @param size 每页数量（默认20）
     * @return 分页的使用日志数据
     */
    @GetMapping("/usage")
    public ApiResult<Page<ApiUsageLog>> getUsage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        Pageable pageable = PageRequest.of(page, size);
        Page<ApiUsageLog> usagePage = monitorService.getUserUsage(userId, pageable);
        return ApiResult.success(usagePage);
    }

    /**
     * 获取当前用户的API使用量统计数据
     *
     * @return 使用量统计响应（包含总调用次数、总token数和分类明细）
     */
    @GetMapping("/stats")
    public ApiResult<UsageStatsResponse> getStats() {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        UsageStatsResponse stats = monitorService.getUserStats(userId);
        return ApiResult.success(stats);
    }

    /**
     * 获取管理员仪表盘的全局统计数据（仅ADMIN角色可访问）
     *
     * @return 全局使用量统计响应
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<UsageStatsResponse> getDashboard() {
        UsageStatsResponse stats = monitorService.getDashboardStats();
        return ApiResult.success(stats);
    }
}
