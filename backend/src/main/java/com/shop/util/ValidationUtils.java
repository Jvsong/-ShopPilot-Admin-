package com.shop.util;

import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * 验证工具类
 * 用于减少重复的验证逻辑
 */
public class ValidationUtils {

    private ValidationUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 验证状态值是否为0或1
     *
     * @param status 状态值
     * @param errorMessage 错误信息
     */
    public static void validateStatus(Integer status, String errorMessage) {
        if (status != null && !status.equals(0) && !status.equals(1)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, errorMessage);
        }
    }

    /**
     * 验证状态值是否为0或1
     *
     * @param status 状态值
     */
    public static void validateStatus(Integer status) {
        validateStatus(status, "状态值不合法，必须为0或1");
    }

    /**
     * 验证字符串不为空
     *
     * @param value 字符串值
     * @param errorMessage 错误信息
     */
    public static void requireNonEmpty(String value, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, errorMessage);
        }
    }

    /**
     * 验证对象不为null
     *
     * @param object 对象
     * @param errorMessage 错误信息
     */
    public static void requireNonNull(Object object, String errorMessage) {
        if (object == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, errorMessage);
        }
    }

    /**
     * 验证集合不为空
     *
     * @param collection 集合
     * @param errorMessage 错误信息
     */
    public static void requireNonEmpty(Collection<?> collection, String errorMessage) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, errorMessage);
        }
    }

    /**
     * 验证数值大于0
     *
     * @param value 数值
     * @param errorMessage 错误信息
     */
    public static void requirePositive(Number value, String errorMessage) {
        if (value == null || value.doubleValue() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, errorMessage);
        }
    }

    /**
     * 验证数值大于等于0
     *
     * @param value 数值
     * @param errorMessage 错误信息
     */
    public static void requireNonNegative(Number value, String errorMessage) {
        if (value == null || value.doubleValue() < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, errorMessage);
        }
    }
}