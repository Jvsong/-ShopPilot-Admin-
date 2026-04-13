package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询请求")
public class UserQueryRequest extends PageRequest {

    @Schema(description = "用户名（模糊查询）", example = "zhang")
    private String username;

    @Schema(description = "邮箱（模糊查询）", example = "example")
    private String email;

    @Schema(description = "手机号（模糊查询）", example = "138")
    private String phone;

    @Schema(description = "状态: 0-禁用, 1-正常", example = "1")
    private Integer status;

    @Schema(description = "用户类型: 1-普通用户, 2-管理员", example = "1")
    private Integer userType;

    @Schema(description = "开始时间", example = "2026-04-01 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-04-30 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "排序字段: create_time, last_login_time", example = "create_time")
    private String sortBy;

    @Schema(description = "排序方向: asc, desc", example = "desc")
    private String sortDirection;
}