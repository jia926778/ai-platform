package com.example.aiplatform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应结果封装类
 * <p>
 * 所有接口统一返回此格式，包含状态码、消息、数据和时间戳。
 * 提供静态工厂方法快速构建成功或失败的响应对象。
 * 使用 {@code @JsonInclude(NON_NULL)} 确保空字段不参与序列化。
 * </p>
 *
 * @param <T> 响应数据的类型
 * @author AI Platform
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    /** 响应状态码（200表示成功） */
    private int code;
    /** 响应消息 */
    private String message;
    /** 响应数据（泛型） */
    private T data;
    /** 响应时间戳（毫秒） */
    private long timestamp;

    /**
     * 构建成功响应（携带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功的ApiResult对象
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("Success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 构建成功响应（自定义消息和数据）
     *
     * @param message 自定义成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功的ApiResult对象
     */
    public static <T> ApiResult<T> success(String message, T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 构建错误响应
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误的ApiResult对象
     */
    public static <T> ApiResult<T> error(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
