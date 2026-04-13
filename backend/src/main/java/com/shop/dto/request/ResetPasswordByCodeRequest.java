package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 通过验证码重置密码请求
 */
@Data
@Schema(description = "通过验证码重置密码请求")
public class ResetPasswordByCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", required = true, example = "zhangsan@example.com")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", required = true, example = "123456")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Length(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    @Schema(description = "新密码", required = true, example = "newpassword123")
    private String newPassword;
}