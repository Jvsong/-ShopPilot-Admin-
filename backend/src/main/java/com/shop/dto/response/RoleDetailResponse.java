package com.shop.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情响应
 */
@Data
public class RoleDetailResponse {

    private Long id;

    private String name;

    private String code;

    private String description;

    private Integer status;

    private List<Long> permissionIds;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;
}
