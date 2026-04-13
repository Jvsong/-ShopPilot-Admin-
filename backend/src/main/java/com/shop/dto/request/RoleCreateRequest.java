package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 角色创建请求
 */
@Data
@Schema(description = "角色创建请求")
public class RoleCreateRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称", example = "系统管理员")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^ROLE_[A-Z_]+$", message = "角色编码必须以ROLE_开头，且只能包含大写字母和下划线")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String code;

    @Size(max = 255, message = "角色描述长度不能超过255个字符")
    @Schema(description = "角色描述", example = "拥有系统所有权限")
    private String description;

    @Schema(description = "状态: 0-禁用, 1-正常", example = "1")
    private Integer status = 1;

    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;
}