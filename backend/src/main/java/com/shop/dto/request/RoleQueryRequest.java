package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询请求")
public class RoleQueryRequest extends PageRequest {

    @Schema(description = "角色名称（模糊查询）", example = "管理员")
    private String name;

    @Schema(description = "角色编码（模糊查询）", example = "ROLE_ADMIN")
    private String code;

    @Schema(description = "状态: 0-禁用, 1-正常", example = "1")
    private Integer status;

    @Schema(description = "排序字段: create_time, update_time", example = "create_time")
    private String sortBy;

    @Schema(description = "排序方向: asc, desc", example = "desc")
    private String sortDirection;
}