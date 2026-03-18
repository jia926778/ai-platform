package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO
 * <p>
 * 封装登录/注册成功后返回给客户端的认证信息，
 * 包括JWT访问令牌、刷新令牌、令牌类型和过期时间。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** JWT访问令牌 */
    private String token;
    /** 刷新令牌（用于获取新的访问令牌） */
    private String refreshToken;
    /** 令牌类型，默认为 Bearer */
    @Builder.Default
    private String tokenType = "Bearer";
    /** 令牌过期时间（秒） */
    private long expiresIn;
}
