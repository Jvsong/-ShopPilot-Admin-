package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 重置密码请求（使用旧密码）
 */
@Data
@Schema(description = "重置密码请求（使用旧密码）")
public class ResetPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名/邮箱/手机号", required = true, example = "zhangsan")
    private String username;

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码", required = true, example = "oldpassword123")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Length(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    @Schema(description = "新密码", required = true, example = "newpassword123")
    private String newPassword;
}