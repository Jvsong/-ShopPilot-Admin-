package com.shop.service;

import com.shop.dto.request.LoginRequest;
import com.shop.dto.request.RegisterRequest;
import com.shop.dto.request.ResetPasswordByCodeRequest;
import com.shop.dto.request.ResetPasswordRequest;
import com.shop.dto.request.SendResetCodeRequest;
import com.shop.dto.response.LoginResponse;
import com.shop.dto.response.UserInfoResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注销
     */
    void logout();

    /**
     * 刷新token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUserInfo();

    /**
     * 用户注册
     */
    Long register(RegisterRequest request);

    /**
     * 重置密码
     */
    void resetPassword(String username, String oldPassword, String newPassword);

    /**
     * 忘记密码 - 发送验证码
     */
    void sendResetPasswordCode(String email);

    /**
     * 忘记密码 - 重置密码
     */
    void resetPasswordByCode(String email, String code, String newPassword);
}