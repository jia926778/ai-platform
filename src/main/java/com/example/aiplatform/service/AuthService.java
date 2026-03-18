package com.example.aiplatform.service;

import com.example.aiplatform.dto.AuthResponse;
import com.example.aiplatform.dto.LoginRequest;
import com.example.aiplatform.dto.RegisterRequest;
import com.example.aiplatform.entity.SysRole;
import com.example.aiplatform.entity.SysUser;
import com.example.aiplatform.exception.BusinessException;
import com.example.aiplatform.repository.SysRoleRepository;
import com.example.aiplatform.repository.SysUserRepository;
import com.example.aiplatform.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证服务类
 * <p>
 * 提供用户注册、登录和令牌刷新功能。
 * 注册时校验用户名/邮箱唯一性，自动分配默认角色，并返回JWT令牌。
 * 登录时通过Spring Security认证管理器验证凭据。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(SysUserRepository userRepository, SysRoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 用户注册
     * <p>
     * 校验用户名和邮箱的唯一性，创建用户并分配默认角色（ROLE_USER），
     * 然后自动登录并返回JWT令牌。
     * </p>
     *
     * @param request 注册请求参数
     * @return 包含JWT令牌的认证响应
     * @throws BusinessException 用户名或邮箱已存在时抛出
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 校验用户名唯一性
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        // 校验邮箱唯一性
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        // 查询默认用户角色
        SysRole userRole = roleRepository.findByRoleCode("ROLE_USER")
                .orElseThrow(() -> new BusinessException("Default role not found"));

        Set<SysRole> roles = new HashSet<>();
        roles.add(userRole);

        // 构建用户实体并保存（密码使用BCrypt加密）
        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .status(1)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // 注册成功后自动登录，生成JWT令牌
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpiration())
                .build();
    }

    /**
     * 用户登录
     * <p>
     * 通过Spring Security的认证管理器验证用户名和密码，
     * 验证通过后生成JWT访问令牌和刷新令牌。
     * </p>
     *
     * @param request 登录请求参数
     * @return 包含JWT令牌的认证响应
     */
    public AuthResponse login(LoginRequest request) {
        // 使用Spring Security认证管理器验证用户名密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpiration())
                .build();
    }

    /**
     * 刷新JWT令牌
     * <p>
     * 验证刷新令牌的有效性，检查用户状态，
     * 然后生成新的访问令牌和刷新令牌对。
     * </p>
     *
     * @param refreshToken 刷新令牌
     * @return 包含新JWT令牌的认证响应
     * @throws BusinessException 令牌无效、用户不存在或账户被禁用时抛出
     */
    public AuthResponse refreshToken(String refreshToken) {
        // 验证刷新令牌的有效性
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(401, "Invalid refresh token");
        }

        // 从令牌中解析用户名并查询用户信息
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "User not found"));

        // 检查用户账户是否启用
        if (user.getStatus() != 1) {
            throw new BusinessException(401, "User account is disabled");
        }

        // 基于用户信息构建认证对象（无需重新输入密码验证）
        List<org.springframework.security.core.GrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> (org.springframework.security.core.GrantedAuthority)
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getRoleCode()))
                .toList();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        new org.springframework.security.core.userdetails.User(
                                user.getUsername(), user.getPassword(), authorities),
                        null,
                        authorities);

        // 生成新的令牌对
        String newToken = jwtTokenProvider.generateToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpiration())
                .build();
    }
}
