package com.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "刷新token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "token类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（毫秒）", example = "86400000")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserInfoResponse userInfo;
}