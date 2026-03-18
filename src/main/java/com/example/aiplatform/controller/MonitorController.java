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

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorService monitorService;
    private final SysUserRepository userRepository;

    public MonitorController(MonitorService monitorService, SysUserRepository userRepository) {
        this.monitorService = monitorService;
        this.userRepository = userRepository;
    }

    @GetMapping("/usage")
    public ApiResult<Page<ApiUsageLog>> getUsage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        Pageable pageable = PageRequest.of(page, size);
        Page<ApiUsageLog> usagePage = monitorService.getUserUsage(userId, pageable);
        return ApiResult.success(usagePage);
    }

    @GetMapping("/stats")
    public ApiResult<UsageStatsResponse> getStats() {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        UsageStatsResponse stats = monitorService.getUserStats(userId);
        return ApiResult.success(stats);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<UsageStatsResponse> getDashboard() {
        UsageStatsResponse stats = monitorService.getDashboardStats();
        return ApiResult.success(stats);
    }
}
