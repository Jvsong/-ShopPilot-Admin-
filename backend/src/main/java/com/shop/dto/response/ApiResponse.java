package com.shop.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一API响应格式
 */
@Data
public class ApiResponse<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * 成功响应（带自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * 成功响应（无数据）
     */
    public static ApiResponse<?> success() {
        return success(null);
    }

    /**
     * 错误响应
     */
    public static ApiResponse<?> error(Integer code, String message) {
        ApiResponse<?> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * 客户端错误
     */
    public static ApiResponse<?> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 未授权
     */
    public static ApiResponse<?> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 禁止访问
     */
    public static ApiResponse<?> forbidden(String message) {
        return error(403, message);
    }

    /**
     * 资源不存在
     */
    public static ApiResponse<?> notFound(String message) {
        return error(404, message);
    }

    /**
     * 服务器错误
     */
    public static ApiResponse<?> serverError(String message) {
        return error(500, message);
    }
}