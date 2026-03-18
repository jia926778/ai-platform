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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SysUserRepository userRepository;

    public UserController(UserService userService, SysUserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ApiResult<UserInfoResponse> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        UserInfoResponse response = userService.getCurrentUser(userId);
        return ApiResult.success(response);
    }

    @PutMapping("/me")
    public ApiResult<UserInfoResponse> updateProfile(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String avatar) {
        Long userId = SecurityUtils.getCurrentUserId(userRepository);
        UserInfoResponse response = userService.updateProfile(userId, nickname, email, avatar);
        return ApiResult.success("Profile updated", response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Page<UserInfoResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserInfoResponse> users = userService.getAllUsers(pageable);
        return ApiResult.success(users);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return ApiResult.success("User status updated", null);
    }
}
