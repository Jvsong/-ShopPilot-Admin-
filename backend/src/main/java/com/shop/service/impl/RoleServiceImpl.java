package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Role;
import com.shop.dao.entity.RolePermission;
import com.shop.dao.entity.User;
import com.shop.dao.entity.UserRole;
import com.shop.dao.mapper.RoleMapper;
import com.shop.dao.mapper.RolePermissionMapper;
import com.shop.dao.mapper.UserMapper;
import com.shop.dao.mapper.UserRoleMapper;
import com.shop.dto.request.RoleCreateRequest;
import com.shop.dto.request.RoleQueryRequest;
import com.shop.dto.request.RoleUpdateRequest;
import com.shop.dto.response.RoleDetailResponse;
import com.shop.dto.response.RoleUserResponse;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Role> listRoles(RoleQueryRequest query) {
        Page<Role> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getIsDeleted, 0);
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Role::getName, query.getName().trim());
        }
        if (StringUtils.hasText(query.getCode())) {
            wrapper.like(Role::getCode, query.getCode().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Role::getStatus, query.getStatus());
        }
        boolean asc = "asc".equalsIgnoreCase(query.getSortDirection());
        if ("update_time".equalsIgnoreCase(query.getSortBy())) {
            wrapper.orderBy(true, asc, Role::getUpdateTime);
        } else {
            wrapper.orderBy(true, asc, Role::getCreateTime);
        }
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public RoleDetailResponse getRoleDetail(Long id) {
        Role role = getActiveRole(id);
        RoleDetailResponse response = new RoleDetailResponse();
        BeanUtils.copyProperties(role, response);
        response.setPermissionIds(getRolePermissions(id));
        return response;
    }

    @Override
    @Transactional
    public Long createRole(RoleCreateRequest request) {
        if (isRoleCodeExists(request.getCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色编码已存在");
        }
        if (isRoleNameExists(request.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色名称已存在");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        role.setIsDeleted(0);
        roleMapper.insert(role);

        assignRolePermissions(role.getId(), request.getPermissionIds());
        return role.getId();
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleUpdateRequest request) {
        Role role = getActiveRole(id);
        if (StringUtils.hasText(request.getName())
                && !Objects.equals(role.getName(), request.getName())
                && isRoleNameExists(request.getName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色名称已存在");
        }

        if (StringUtils.hasText(request.getName())) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }
        roleMapper.updateById(role);

        if (request.getPermissionIds() != null) {
            assignRolePermissions(id, request.getPermissionIds());
        }
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = getActiveRole(id);
        role.setIsDeleted(1);
        roleMapper.updateById(role);

        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
    }

    @Override
    public void updateRoleStatus(Long id, Integer status) {
        Role role = getActiveRole(id);
        role.setStatus(status);
        roleMapper.updateById(role);
    }

    @Override
    public void batchUpdateRoleStatus(List<Long> ids, Integer status) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            updateRoleStatus(id, status);
        }
    }

    @Override
    public boolean isRoleCodeExists(String code) {
        return roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, code)
                .eq(Role::getIsDeleted, 0)) > 0;
    }

    @Override
    public boolean isRoleNameExists(String name) {
        return roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getName, name)
                .eq(Role::getIsDeleted, 0)) > 0;
    }

    @Override
    @Transactional
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        getActiveRole(roleId);
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (Long permissionId : permissionIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList())) {
            RolePermission relation = new RolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            rolePermissionMapper.insert(relation);
        }
    }

    @Override
    @Transactional
    public void removeRolePermissions(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)
                .in(RolePermission::getPermissionId, permissionIds));
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        getActiveRole(roleId);
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId))
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleUserResponse> getRoleUsers(Long roleId) {
        getActiveRole(roleId);
        List<Long> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId))
                .stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(userIds).stream().map(user -> {
            RoleUserResponse response = new RoleUserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            response.setStatus(user.getStatus());
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRoleUsers(Long roleId, List<Long> userIds) {
        getActiveRole(roleId);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId));
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList())) {
            UserRole relation = new UserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        }
    }

    @Override
    public List<Role> getAllAvailableRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getIsDeleted, 0)
                .eq(Role::getStatus, 1)
                .orderByAsc(Role::getCreateTime));
    }

    private Role getActiveRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色不存在");
        }
        return role;
    }
}
