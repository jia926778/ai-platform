package com.example.aiplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 请求日志查询参数DTO
 */
@Data
public class RequestLogQueryDTO {
    /** 用户ID（管理员筛选用） */
    private Long userId;
    /** 关键词（ES全文检索） */
    private String keyword;
    /** API类型：CHAT / SUMMARY / CODEGEN */
    private String apiType;
    /** 请求状态：SUCCESS / FAILED */
    private String status;
    /** AI服务提供商 */
    private String provider;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 页码（从0开始） */
    private Integer page = 0;
    /** 每页大小 */
    private Integer size = 20;
}
