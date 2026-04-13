package com.shop.exception;

import com.shop.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数验证异常: {}", message);
        return ApiResponse.badRequest(message);
    }

    /**
     * 处理参数验证异常（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().iterator().next().getMessage();
        log.warn("参数验证异常: {}", message);
        return ApiResponse.badRequest(message);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponse<?> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证异常: {}", e.getMessage());
        return ApiResponse.unauthorized("用户名或密码错误");
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限异常: {}", e.getMessage());
        return ApiResponse.forbidden("没有访问权限");
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ApiResponse.serverError("系统内部错误");
    }
}