package com.example.aiplatform.controller;

import com.example.aiplatform.dto.*;
import com.example.aiplatform.repository.SysUserRepository;
import com.example.aiplatform.service.RequestLogService;
import com.example.aiplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 请求日志控制器
 * <p>
 * 提供请求日志查询、全文检索和高频问题统计接口。
 * 普通用户可查看自己的历史请求，管理员可进行全局搜索和分析。
 * </p>
 */
@RestController
@RequestMapping("/api/request-logs")
@RequiredArgsConstructor
public class RequestLogController {

    private final RequestLogService requestLogService;
    private final SysUserRepository userRepository;

    /**
     * 用户查看自己的历史请求
     */
    @GetMapping("/my")
    public ApiResult<Page<RequestLogResponse>> getMyRequests(
            @RequestParam(required = false) String apiType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        Page<RequestLogResponse> result = requestLogService.getUserRequests(userId, apiType, PageRequest.of(page, size));
        return ApiResult.success(result);
    }

    /**
     * 查看请求详情
     */
    @GetMapping("/{id}")
    public ApiResult<RequestLogResponse> getRequestDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        // 从安全上下文中检查是否具有管理员角色
        boolean hasAdminRole = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        RequestLogResponse result = requestLogService.getRequestDetail(id, userId, hasAdminRole);
        return ApiResult.success(result);
    }

    /**
     * 管理员：按条件筛选请求日志（MySQL查询）
     */
    @GetMapping("/admin/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Page<RequestLogResponse>> adminFilterRequests(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String apiType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        RequestLogQueryDTO query = new RequestLogQueryDTO();
        query.setUserId(userId);
        query.setApiType(apiType);
        query.setStatus(status);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPage(page);
        query.setSize(size);
        return ApiResult.success(requestLogService.adminFilterRequests(query));
    }

    /**
     * 管理员：ES全文检索请求日志
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<List<RequestLogResponse>> searchRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String apiType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        RequestLogQueryDTO query = new RequestLogQueryDTO();
        query.setKeyword(keyword);
        query.setUserId(userId);
        query.setApiType(apiType);
        query.setStatus(status);
        query.setProvider(provider);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPage(page);
        query.setSize(size);
        return ApiResult.success(requestLogService.searchByKeyword(query));
    }

    /**
     * 管理员：高频问题统计（基于ES聚合分析）
     */
    @GetMapping("/admin/frequent-questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<List<FrequentQuestionDTO>> getFrequentQuestions(
            @RequestParam(required = false) String apiType,
            @RequestParam(defaultValue = "20") int topN,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ApiResult.success(requestLogService.getFrequentQuestions(apiType, topN, startTime, endTime));
    }
}
