package com.shop.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.dao.entity.Permission;
import com.shop.dao.mapper.PermissionMapper;
import com.shop.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionMapper permissionMapper;

    @GetMapping("")
    public ApiResponse<List<Permission>> listPermissions() {
        List<Permission> permissions = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getIsDeleted, 0)
                .orderByAsc(Permission::getSortOrder)
                .orderByAsc(Permission::getId));
        return ApiResponse.success(permissions);
    }
}
