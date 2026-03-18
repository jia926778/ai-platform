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

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService summaryService;
    private final SysUserRepository userRepository;

    public SummaryController(SummaryService summaryService, SysUserRepository userRepository) {
        this.summaryService = summaryService;
        this.userRepository = userRepository;
    }

    @PostMapping("/generate")
    public ApiResult<SummaryResponse> generateSummary(@Valid @RequestBody SummaryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        SummaryResponse response = summaryService.generateSummary(userId, request);
        return ApiResult.success(response);
    }
}
