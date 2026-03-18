package com.example.aiplatform.controller;

import com.example.aiplatform.dto.ApiResult;
import com.example.aiplatform.dto.UserInfoResponse;
import com.example.aiplatform.repository.SysUserRepository;
import com.example.aiplatform.service.UserService;
import com.example.aiplatform.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器
 * <p>
 * 提供用户信息管理的REST API接口。
 * 普通用户可查看和更新个人资料，管理员可查看所有用户列表和修改用户状态。
 * 需要JWT认证，用户管理接口需要ADMIN角色。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SysUserRepository userRepository;

    public UserController(UserService userService, SysUserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * 获取当前登录用户的个人信息
     *
     * @return 用户信息响应
     */
    @GetMapping("/me")
    public ApiResult<UserInfoResponse> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        UserInfoResponse response = userService.getCurrentUser(userId);
        return ApiResult.success(response);
    }

    /**
     * 更新当前用户的个人资料
     *
     * @param nickname 新昵称（可选）
     * @param email    新邮箱（可选）
     * @param avatar   新头像URL（可选）
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    public ApiResult<UserInfoResponse> updateProfile(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String avatar) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        UserInfoResponse response = userService.updateProfile(userId, nickname, email, avatar);
        return ApiResult.success("Profile updated", response);
    }

    /**
     * 分页获取所有用户列表（仅ADMIN角色可访问）
     *
     * @param page 页码（从0开始，默认0）
     * @param size 每页数量（默认20）
     * @return 分页的用户信息
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Page<UserInfoResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserInfoResponse> users = userService.getAllUsers(pageable);
        return ApiResult.success(users);
    }

    /**
     * 更新用户账户状态（仅ADMIN角色可访问）
     *
     * @param id     用户ID
     * @param status 新状态（1-启用，0-禁用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return ApiResult.success("User status updated", null);
    }
}
