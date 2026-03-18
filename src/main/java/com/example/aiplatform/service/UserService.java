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

@Service
public class UserService {

    private final SysUserRepository userRepository;

    public UserService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfoResponse getCurrentUser(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        return toUserInfoResponse(user);
    }

    @Transactional
    public UserInfoResponse updateProfile(Long userId, String nickname, String email, String avatar) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (email != null && !email.isEmpty()) {
            if (userRepository.existsByEmail(email) && !email.equals(user.getEmail())) {
                throw new BusinessException("Email already exists");
            }
            user.setEmail(email);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return toUserInfoResponse(user);
    }

    public Page<UserInfoResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserInfoResponse);
    }

    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

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
