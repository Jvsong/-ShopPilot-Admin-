package com.shop.exception;

import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 构造业务异常
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常（默认错误码500）
     */
    public BusinessException(String message) {
        this(500, message);
    }

    /**
     * 构造业务异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 构造业务异常（使用ErrorCode枚举）
     */
    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 构造业务异常（使用ErrorCode枚举和自定义消息）
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        this(errorCode.getCode(), customMessage);
    }
}