package com.shop.exception;

/**
 * 错误码枚举
 */
public enum ErrorCode {

    // 成功
    SUCCESS(200, "success"),

    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    PARAM_ERROR(40001, "参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误
    USER_NOT_EXIST(10001, "用户不存在"),
    USER_DISABLED(10002, "用户已被禁用"),
    USER_LOCKED(10003, "用户已被锁定"),
    PASSWORD_ERROR(10004, "密码错误"),
    LOGIN_FAILED(10005, "登录失败"),
    TOKEN_EXPIRED(10006, "Token已过期"),
    TOKEN_INVALID(10007, "Token无效"),

    // 商品错误
    PRODUCT_NOT_EXIST(20001, "商品不存在"),
    PRODUCT_DISABLED(20002, "商品已下架"),
    PRODUCT_STOCK_INSUFFICIENT(20003, "商品库存不足"),
    PRODUCT_NOT_FOUND(20004, "商品不存在或已被删除"),
    PRODUCT_NAME_EXISTS(20005, "商品名称已存在"),
    PRODUCT_ATTRIBUTE_ERROR(20006, "商品属性错误"),

    // 订单错误
    ORDER_NOT_EXIST(30001, "订单不存在"),
    ORDER_STATUS_ERROR(30002, "订单状态错误"),
    ORDER_CANCEL_FAILED(30003, "订单取消失败"),

    // 系统错误
    SYSTEM_ERROR(500, "系统内部错误"),
    DATABASE_ERROR(50001, "数据库操作失败"),
    FILE_UPLOAD_ERROR(50002, "文件上传失败"),
    NETWORK_ERROR(50003, "网络连接失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}