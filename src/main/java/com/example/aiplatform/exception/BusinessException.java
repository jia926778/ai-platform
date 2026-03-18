package com.example.aiplatform.exception;

import lombok.Getter;

/**
 * 业务异常类
 * <p>
 * 自定义运行时异常，用于表示业务逻辑层面的错误。
 * 携带HTTP状态码（默认400）和错误消息，
 * 由 {@link GlobalExceptionHandler} 统一捕获并返回标准错误响应。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /** HTTP错误状态码 */
    private final int code;

    /**
     * 创建业务异常（默认状态码400）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 创建业务异常（自定义状态码）
     *
     * @param code    HTTP错误状态码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
