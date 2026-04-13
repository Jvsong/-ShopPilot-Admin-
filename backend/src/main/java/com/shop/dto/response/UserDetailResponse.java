package com.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情响应
 */
@Data
@Schema(description = "用户详情响应")
public class UserDetailResponse {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态: 0-禁用, 1-正常", example = "1")
    private Integer status;

    @Schema(description = "用户类型: 1-普通用户, 2-管理员", example = "1")
    private Integer userType;

    @Schema(description = "登录失败次数", example = "0")
    private Integer loginFailCount;

    @Schema(description = "锁定时间")
    private LocalDateTime lockTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "备注")
    private String remark;
}