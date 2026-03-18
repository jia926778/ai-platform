package com.example.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求DTO
 * <p>
 * 封装用户登录时提交的用户名和密码信息。
 * 使用 JSR 380 校验注解确保必填字段不为空。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /** 登录用户名（必填） */
    @NotBlank(message = "Username is required")
    private String username;

    /** 登录密码（必填） */
    @NotBlank(message = "Password is required")
    private String password;
}
