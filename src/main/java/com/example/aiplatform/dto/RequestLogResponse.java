package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 请求日志响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private String apiType;
    private String model;
    private String provider;
    private String requestContent;
    private String responseContent;
    private Integer totalTokens;
    private Long durationMs;
    private String status;
    private String errorMessage;
    private Long conversationId;
    private LocalDateTime createdAt;
}
