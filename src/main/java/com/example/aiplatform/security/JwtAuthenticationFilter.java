package com.example.aiplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * <p>
 * 继承 {@link OncePerRequestFilter}，在每个HTTP请求中执行一次JWT认证。
 * 从请求头中提取Bearer令牌，验证其有效性，并将认证信息设置到Spring Security上下文中。
 * 认证相关的请求路径（/api/auth/**）会被跳过，不进行JWT验证。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT令牌提供者 */
    private final JwtTokenProvider jwtTokenProvider;
    /** 自定义用户详情服务 */
    private final CustomUserDetailsService userDetailsService;
    /** URL路径匹配器 */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 执行JWT认证过滤逻辑
     * <p>
     * 对于认证路径直接放行；其他路径提取并验证JWT令牌，
     * 验证通过后将用户认证信息设置到SecurityContext中。
     * </p>
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        // 认证相关接口直接放行，不需要JWT验证
        if (pathMatcher.match("/api/auth/**", path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头中提取JWT令牌并验证
        String token = extractTokenFromRequest(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 从令牌中解析用户名，加载用户详情
            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 构建认证对象并设置到安全上下文
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求的Authorization头中提取JWT令牌
     *
     * @param request HTTP请求
     * @return JWT令牌字符串，如果不存在则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 检查是否为Bearer类型的令牌
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
