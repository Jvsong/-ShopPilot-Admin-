package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 用户更新请求
 */
@Data
@Schema(description = "用户更新请求")
public class UserUpdateRequest {

    @Length(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Length(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "密码", example = "newpassword123")
    private String password;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "用户类型: 1-普通用户, 2-管理员, 3-商家", example = "1")
    private Integer userType;

    @Schema(description = "状态: 0-禁用, 1-正常", example = "1")
    private Integer status;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "备注")
    private String remark;
}
