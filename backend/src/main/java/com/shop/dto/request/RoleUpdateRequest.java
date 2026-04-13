package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * 角色更新请求
 */
@Data
@Schema(description = "角色更新请求")
public class RoleUpdateRequest {

    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称", example = "系统管理员")
    private String name;

    @Size(max = 255, message = "角色描述长度不能超过255个字符")
    @Schema(description = "角色描述", example = "拥有系统所有权限")
    private String description;

    @Schema(description = "状态: 0-禁用, 1-正常", example = "1")
    private Integer status;

    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;
}