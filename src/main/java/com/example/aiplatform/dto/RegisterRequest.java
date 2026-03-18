package com.example.aiplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册请求DTO
 * <p>
 * 封装用户注册时提交的账户信息，包括用户名、密码、邮箱和昵称。
 * 使用 JSR 380 校验注解对各字段进行格式和长度验证。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /** 用户名（必填，长度3-50个字符） */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /** 密码（必填，长度6-100个字符） */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /** 邮箱地址（可选，需符合邮箱格式） */
    @Email(message = "Invalid email format")
    private String email;

    /** 用户昵称（可选，最大50个字符） */
    @Size(max = 50, message = "Nickname must not exceed 50 characters")
    private String nickname;
}
