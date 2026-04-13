package com.shop.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Role;
import com.shop.dto.request.RoleCreateRequest;
import com.shop.dto.request.RoleQueryRequest;
import com.shop.dto.request.RoleUpdateRequest;
import com.shop.dto.response.ApiResponse;
import com.shop.dto.response.PageResponse;
import com.shop.dto.response.RoleDetailResponse;
import com.shop.dto.response.RoleUserResponse;
import com.shop.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Validated
@Tag(name = "角色管理", description = "后台角色接口")
public class AdminRoleController {

    private final RoleService roleService;

    @GetMapping("")
    public ApiResponse<PageResponse<Role>> listRoles(@Valid RoleQueryRequest request) {
        Page<Role> page = roleService.listRoles(request);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleDetailResponse> getRoleDetail(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleDetail(id));
    }

    @PostMapping("")
    public ApiResponse<Long> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        roleService.updateRole(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateRoleStatus(@PathVariable Long id, @RequestParam Integer status) {
        roleService.updateRoleStatus(id, status);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/permissions")
    public ApiResponse<List<Long>> getRolePermissions(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRolePermissions(id));
    }

    @PutMapping("/{id}/permissions")
    public ApiResponse<Void> assignRolePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignRolePermissions(id, permissionIds);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/users")
    public ApiResponse<List<RoleUserResponse>> getRoleUsers(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleUsers(id));
    }

    @PutMapping("/{id}/users")
    public ApiResponse<Void> assignRoleUsers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        roleService.assignRoleUsers(id, userIds);
        return ApiResponse.success(null);
    }
}
