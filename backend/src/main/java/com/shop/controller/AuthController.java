package com.shop.controller;

import com.shop.dto.request.*;
import com.shop.dto.response.ApiResponse;
import com.shop.dto.response.LoginResponse;
import com.shop.dto.response.UserInfoResponse;
import com.shop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、注册、注销、密码重置等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<Long> register(@Validated @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return ApiResponse.success("注册成功", userId);
    }

    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public ApiResponse<?> logout() {
        authService.logout();
        return ApiResponse.success("注销成功");
    }

    @Operation(summary = "刷新token")
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public ApiResponse<UserInfoResponse> getUserInfo() {
        UserInfoResponse response = authService.getCurrentUserInfo();
        return ApiResponse.success(response);
    }

    @Operation(summary = "重置密码（使用旧密码）")
    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("密码重置成功");
    }

    @Operation(summary = "发送重置密码验证码")
    @PostMapping("/send-reset-code")
    public ApiResponse<?> sendResetPasswordCode(@Validated @RequestBody SendResetCodeRequest request) {
        authService.sendResetPasswordCode(request.getEmail());
        return ApiResponse.success("验证码发送成功");
    }

    @Operation(summary = "通过验证码重置密码")
    @PostMapping("/reset-password-by-code")
    public ApiResponse<?> resetPasswordByCode(@Validated @RequestBody ResetPasswordByCodeRequest request) {
        authService.resetPasswordByCode(request.getEmail(), request.getCode(), request.getNewPassword());
        return ApiResponse.success("密码重置成功");
    }
}