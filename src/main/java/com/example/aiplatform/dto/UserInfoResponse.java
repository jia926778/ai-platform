package com.example.aiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应DTO
 * <p>
 * 封装用户个人信息查询的返回结果，
 * 包括基本信息、角色列表和注册时间。
 * 不包含密码等敏感信息。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 邮箱地址 */
    private String email;
    /** 用户昵称 */
    private String nickname;
    /** 头像URL */
    private String avatar;
    /** 用户角色编码列表 */
    private List<String> roles;
    /** 账户创建时间 */
    private LocalDateTime createdAt;
}
