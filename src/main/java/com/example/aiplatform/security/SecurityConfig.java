package com.example.aiplatform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置类
 * <p>
 * 配置HTTP安全策略，包括：禁用CSRF（因为使用JWT无状态认证）、
 * 设置会话管理为无状态模式、定义URL访问权限规则、
 * 注册JWT认证过滤器。同时提供认证管理器和密码编码器的Bean。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** JWT认证过滤器 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置安全过滤器链
     * <p>
     * 定义各URL的访问权限规则，并将JWT过滤器添加到UsernamePasswordAuthenticationFilter之前。
     * </p>
     *
     * @param http HttpSecurity配置对象
     * @return 构建的SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（JWT无状态认证不需要CSRF防护）
                .csrf(AbstractHttpConfigurer::disable)
                // 设置会话策略为无状态（不创建HttpSession）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 认证接口允许匿名访问
                        .requestMatchers("/api/auth/**").permitAll()
                        // Swagger文档接口允许匿名访问
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // 监控面板仅管理员可访问
                        .requestMatchers("/api/monitor/dashboard").hasRole("ADMIN")
                        // 个人信息接口需要认证
                        .requestMatchers("/api/users/me").authenticated()
                        // 用户管理接口仅管理员可访问
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 在用户名密码认证过滤器之前添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置认证管理器Bean
     *
     * @param authenticationConfiguration 认证配置
     * @return 认证管理器实例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 配置密码编码器Bean
     * <p>
     * 使用BCrypt算法对密码进行加密和验证。
     * </p>
     *
     * @return BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
