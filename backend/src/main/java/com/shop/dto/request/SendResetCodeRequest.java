package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 发送重置密码验证码请求
 */
@Data
@Schema(description = "发送重置密码验证码请求")
public class SendResetCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", required = true, example = "zhangsan@example.com")
    private String email;
}