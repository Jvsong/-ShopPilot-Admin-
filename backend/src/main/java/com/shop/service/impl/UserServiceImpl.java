package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.User;
import com.shop.dao.entity.UserRole;
import com.shop.dao.mapper.UserMapper;
import com.shop.dao.mapper.UserRoleMapper;
import com.shop.dto.request.UserCreateRequest;
import com.shop.dto.request.UserQueryRequest;
import com.shop.dto.request.UserUpdateRequest;
import com.shop.dto.response.UserDetailResponse;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.shop.util.QueryBuilder;
import com.shop.util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> listUsers(UserQueryRequest query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 使用QueryBuilder构建查询条件
        QueryBuilder.Builder.of(wrapper)
                .likeIfPresent(query.getUsername(), User::getUsername)
                .likeIfPresent(query.getEmail(), User::getEmail)
                .likeIfPresent(query.getPhone(), User::getPhone)
                .eqIfNotNull(query.getStatus(), User::getStatus)
                .eqIfNotNull(query.getUserType(), User::getUserType)
                .geIfNotNull(query.getStartTime(), User::getCreateTime)
                .leIfNotNull(query.getEndTime(), User::getCreateTime)
                .notDeleted(User::getIsDeleted);

        // 排序处理
        if (StringUtils.hasText(query.getSortBy())) {
            String sortBy = query.getSortBy();
            boolean asc = "asc".equalsIgnoreCase(query.getSortDirection());
            switch (sortBy) {
                case "last_login_time":
                    wrapper.orderBy(true, asc, User::getLastLoginTime);
                    break;
                case "create_time":
                default:
                    wrapper.orderBy(true, asc, User::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(User::getCreateTime);
        }

        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public UserDetailResponse getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        UserDetailResponse response = new UserDetailResponse();
        BeanUtils.copyProperties(user, response);

        // 获取用户角色
        List<String> roles = getUserRoles(id);
        response.setRoles(roles);

        return response;
    }

    @Override
    @Transactional
    public Long createUser(UserCreateRequest request) {
        // 检查用户名是否重复
        if (isUsernameExists(request.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }

        // 检查邮箱是否重复
        if (StringUtils.hasText(request.getEmail()) && isEmailExists(request.getEmail())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(request, user);

        // 密码加密
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1); // 默认启用
        user.setIsDeleted(0);
        user.setLoginFailCount(0);
        user.setUserType(request.getUserType() != null ? request.getUserType() : 1); // 默认普通用户

        userMapper.insert(user);

        // 分配用户角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignUserRoles(user.getId(), request.getRoleIds());
        }

        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        // 如果修改了用户名，检查是否重复
        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            if (isUsernameExists(request.getUsername())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已存在");
            }
        }

        // 如果修改了邮箱，检查是否重复
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (isEmailExists(request.getEmail())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱已存在");
            }
        }

        // 更新用户信息
        BeanUtils.copyProperties(request, user, "id", "password", "isDeleted", "createTime", "createBy");

        // 如果修改了密码
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateById(user);

        // 更新用户角色
        if (request.getRoleIds() != null) {
            // 先移除所有角色
            LambdaQueryWrapper<UserRole> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(UserRole::getUserId, id);
            userRoleMapper.delete(deleteWrapper);

            // 重新分配角色
            assignUserRoles(id, request.getRoleIds());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        // 软删除用户
        user.setIsDeleted(1);
        userMapper.updateById(user);

        // 移除用户角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        userRoleMapper.delete(wrapper);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        ValidationUtils.validateStatus(status, "用户状态值不合法");

        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void batchUpdateUserStatus(List<Long> ids, Integer status) {
        ValidationUtils.requireNonEmpty(ids, "用户ID列表不能为空");
        ValidationUtils.validateStatus(status, "用户状态值不合法");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getId, ids)
                .eq(User::getIsDeleted, 0);

        List<User> users = userMapper.selectList(wrapper);
        for (User user : users) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能为空");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginFailCount(0); // 重置登录失败次数
        user.setLockTime(null); // 清除锁定时间
        userMapper.updateById(user);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email)
                .eq(User::getIsDeleted, 0);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public boolean isUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isEmailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email)
                .eq(User::getIsDeleted, 0);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return userRoleMapper.selectRoleNamesByUserId(userId);
    }

    @Override
    @Transactional
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    @Transactional
    public void removeUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId)
                .in(UserRole::getRoleId, roleIds);
        userRoleMapper.delete(wrapper);
    }

    @Override
    public long getOnlineUserCount() {
        // 简单实现：最近30分钟内有登录记录的用户
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getLastLoginTime, threshold)
                .eq(User::getStatus, 1)
                .eq(User::getIsDeleted, 0);
        return userMapper.selectCount(wrapper);
    }

    @Override
    public long getTodayNewUserCount() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getCreateTime, todayStart)
                .eq(User::getIsDeleted, 0);
        return userMapper.selectCount(wrapper);
    }
}