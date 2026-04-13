package com.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Role;
import com.shop.dto.request.RoleCreateRequest;
import com.shop.dto.request.RoleQueryRequest;
import com.shop.dto.request.RoleUpdateRequest;
import com.shop.dto.response.RoleDetailResponse;
import com.shop.dto.response.RoleUserResponse;

import java.util.List;

public interface RoleService {

    Page<Role> listRoles(RoleQueryRequest query);

    RoleDetailResponse getRoleDetail(Long id);

    Long createRole(RoleCreateRequest request);

    void updateRole(Long id, RoleUpdateRequest request);

    void deleteRole(Long id);

    void updateRoleStatus(Long id, Integer status);

    void batchUpdateRoleStatus(List<Long> ids, Integer status);

    boolean isRoleCodeExists(String code);

    boolean isRoleNameExists(String name);

    void assignRolePermissions(Long roleId, List<Long> permissionIds);

    void removeRolePermissions(Long roleId, List<Long> permissionIds);

    List<Long> getRolePermissions(Long roleId);

    List<RoleUserResponse> getRoleUsers(Long roleId);

    void assignRoleUsers(Long roleId, List<Long> userIds);

    List<Role> getAllAvailableRoles();
}
