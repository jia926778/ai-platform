package com.example.aiplatform.ratelimit;

import com.example.aiplatform.dto.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 限流拦截器
 * <p>
 * 实现 {@link HandlerInterceptor}，在请求到达控制器之前进行限流检查。
 * 根据已认证用户名或客户端IP地址作为限流标识，
 * 超过限流阈值时返回429错误（请求过多）。
 * 限流参数通过配置文件设置。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    /** 默认限流阈值（每个时间窗口内允许的最大请求数） */
    @Value("${app.rate-limit.default-limit:100}")
    private int defaultLimit;

    /** 默认限流时间窗口（秒） */
    @Value("${app.rate-limit.default-duration:3600}")
    private int defaultDuration;

    public RateLimitInterceptor(RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    /**
     * 请求预处理：执行限流检查
     * <p>
     * 在请求到达控制器之前检查是否超过限流阈值。
     * 超限时直接返回429错误响应，不再继续处理请求。
     * </p>
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器对象
     * @return 允许请求返回true，超限返回false
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String key = resolveKey(request);
        if (!rateLimiter.isAllowed(key, defaultLimit, defaultDuration)) {
            // 超过限流阈值，返回429错误
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ApiResult<Void> result = ApiResult.error(429, "Rate limit exceeded. Please try again later.");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }
        return true;
    }

    /**
     * 解析限流标识Key
     * <p>
     * 已认证用户使用 "user:用户名:请求路径" 作为Key，
     * 未认证用户使用 "ip:客户端IP:请求路径" 作为Key。
     * 支持通过X-Forwarded-For头获取代理后的真实IP。
     * </p>
     *
     * @param request HTTP请求
     * @return 限流标识Key
     */
    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 已认证用户使用用户名作为限流标识
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName() + ":" + request.getRequestURI();
        }
        // 未认证用户使用IP地址作为限流标识
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return "ip:" + ip + ":" + request.getRequestURI();
    }
}
