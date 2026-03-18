package com.example.aiplatform.controller;

import com.example.aiplatform.dto.ApiResult;
import com.example.aiplatform.dto.AuthResponse;
import com.example.aiplatform.dto.LoginRequest;
import com.example.aiplatform.dto.RegisterRequest;
import com.example.aiplatform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * <p>
 * 提供用户认证相关的REST API接口，包括注册、登录和令牌刷新。
 * 所有接口路径以 /api/auth 为前缀，无需JWT认证即可访问。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册接口
     *
     * @param request 注册请求参数（用户名、密码、邮箱、昵称）
     * @return 包含JWT令牌的认证响应
     */
    @PostMapping("/register")
    public ApiResult<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResult.success("Registration successful", response);
    }

    /**
     * 用户登录接口
     *
     * @param request 登录请求参数（用户名、密码）
     * @return 包含JWT令牌的认证响应
     */
    @PostMapping("/login")
    public ApiResult<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResult.success("Login successful", response);
    }

    /**
     * 刷新JWT令牌接口
     *
     * @param refreshToken 刷新令牌
     * @return 包含新JWT令牌的认证响应
     */
    @PostMapping("/refresh")
    public ApiResult<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ApiResult.success("Token refreshed", response);
    }
}
