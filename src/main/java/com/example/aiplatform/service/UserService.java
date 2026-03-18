package com.example.aiplatform.service;

import com.example.aiplatform.dto.UserInfoResponse;
import com.example.aiplatform.entity.SysUser;
import com.example.aiplatform.exception.BusinessException;
import com.example.aiplatform.repository.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * <p>
 * 提供用户信息管理功能，包括查询当前用户信息、更新个人资料、
 * 获取所有用户列表（管理员）和修改用户状态（管理员）。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Service
public class UserService {

    private final SysUserRepository userRepository;

    public UserService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 获取当前登录用户的详细信息
     *
     * @param userId 用户ID
     * @return 用户信息响应DTO
     * @throws BusinessException 用户不存在时抛出
     */
    public UserInfoResponse getCurrentUser(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        return toUserInfoResponse(user);
    }

    /**
     * 更新用户个人资料
     * <p>
     * 支持更新昵称、邮箱和头像，仅更新非空的字段。
     * 更新邮箱时会校验新邮箱的唯一性。
     * </p>
     *
     * @param userId   用户ID
     * @param nickname 新昵称（可选）
     * @param email    新邮箱（可选）
     * @param avatar   新头像URL（可选）
     * @return 更新后的用户信息
     * @throws BusinessException 用户不存在或邮箱已被占用时抛出
     */
    @Transactional
    public UserInfoResponse updateProfile(Long userId, String nickname, String email, String avatar) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        // 更新昵称（非空时）
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        // 更新邮箱（非空时，需校验唯一性）
        if (email != null && !email.isEmpty()) {
            if (userRepository.existsByEmail(email) && !email.equals(user.getEmail())) {
                throw new BusinessException("Email already exists");
            }
            user.setEmail(email);
        }
        // 更新头像
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return toUserInfoResponse(user);
    }

    /**
     * 分页获取所有用户列表（管理员功能）
     *
     * @param pageable 分页参数
     * @return 分页的用户信息
     */
    public Page<UserInfoResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserInfoResponse);
    }

    /**
     * 更新用户账户状态（管理员功能）
     *
     * @param userId 用户ID
     * @param status 新状态（1-启用，0-禁用）
     * @throws BusinessException 用户不存在时抛出
     */
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 将用户实体转换为用户信息响应DTO
     * <p>
     * 提取用户基本信息和角色编码列表，排除密码等敏感字段。
     * </p>
     *
     * @param user 用户实体
     * @return 用户信息响应DTO
     */
    private UserInfoResponse toUserInfoResponse(SysUser user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream()
                        .map(role -> role.getRoleCode())
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
