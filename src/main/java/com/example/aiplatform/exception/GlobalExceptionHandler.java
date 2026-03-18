package com.example.aiplatform.exception;

import com.example.aiplatform.dto.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 使用 {@code @RestControllerAdvice} 拦截所有控制器抛出的异常，
 * 统一转换为 {@link ApiResult} 格式的错误响应。
 * 支持处理业务异常、参数校验异常、权限异常、认证异常和其他未知异常。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 包含错误码和错误消息的响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleBusinessException(BusinessException e) {
        logger.warn("Business exception: {}", e.getMessage());
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（JSR 380验证失败）
     *
     * @param e 参数校验异常
     * @return 包含所有校验错误信息的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 收集所有字段的校验错误消息，用逗号分隔
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("Validation error: {}", errors);
        return ApiResult.error(400, errors);
    }

    /**
     * 处理访问拒绝异常（权限不足）
     *
     * @param e 访问拒绝异常
     * @return 403错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<Void> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("Access denied: {}", e.getMessage());
        return ApiResult.error(403, "Access denied");
    }

    /**
     * 处理认证异常（登录失败、令牌无效等）
     *
     * @param e 认证异常
     * @return 401错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResult<Void> handleAuthenticationException(AuthenticationException e) {
        logger.warn("Authentication failed: {}", e.getMessage());
        return ApiResult.error(401, "Authentication failed: " + e.getMessage());
    }

    /**
     * 处理所有未被捕获的异常（兜底处理）
     *
     * @param e 未知异常
     * @return 500内部错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Void> handleException(Exception e) {
        logger.error("Unexpected error", e);
        return ApiResult.error(500, "Internal server error: " + e.getMessage());
    }
}
