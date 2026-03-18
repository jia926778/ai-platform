package com.example.aiplatform.controller;

import com.example.aiplatform.dto.ApiResult;
import com.example.aiplatform.dto.SummaryRequest;
import com.example.aiplatform.dto.SummaryResponse;
import com.example.aiplatform.repository.SysUserRepository;
import com.example.aiplatform.service.SummaryService;
import com.example.aiplatform.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文本摘要控制器
 * <p>
 * 提供文本摘要生成的REST API接口。
 * 接收用户提交的长文本，调用AI模型生成精简摘要。
 * 需要JWT认证。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService summaryService;
    private final SysUserRepository userRepository;

    public SummaryController(SummaryService summaryService, SysUserRepository userRepository) {
        this.summaryService = summaryService;
        this.userRepository = userRepository;
    }

    /**
     * 生成文本摘要
     *
     * @param request 摘要请求参数（原始文本和可选的最大长度限制）
     * @return 摘要响应（包含摘要文本和统计信息）
     */
    @PostMapping("/generate")
    public ApiResult<SummaryResponse> generateSummary(@Valid @RequestBody SummaryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        SummaryResponse response = summaryService.generateSummary(userId, request);
        return ApiResult.success(response);
    }
}
