package com.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.User;
import com.shop.dto.request.UserCreateRequest;
import com.shop.dto.request.UserQueryRequest;
import com.shop.dto.request.UserUpdateRequest;
import com.shop.dto.response.UserDetailResponse;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 分页查询用户列表
     */
    Page<User> listUsers(UserQueryRequest query);

    /**
     * 获取用户详情
     */
    UserDetailResponse getUserDetail(Long id);

    /**
     * 创建用户
     */
    Long createUser(UserCreateRequest request);

    /**
     * 更新用户
     */
    void updateUser(Long id, UserUpdateRequest request);

    /**
     * 删除用户（软删除）
     */
    void deleteUser(Long id);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 批量更新用户状态
     */
    void batchUpdateUserStatus(List<Long> ids, Integer status);

    /**
     * 重置用户密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User getUserByEmail(String email);

    /**
     * 检查用户名是否已存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExists(String email);

    /**
     * 获取用户角色列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 分配用户角色
     */
    void assignUserRoles(Long userId, List<Long> roleIds);

    /**
     * 移除用户角色
     */
    void removeUserRoles(Long userId, List<Long> roleIds);

    /**
     * 获取在线用户数量
     */
    long getOnlineUserCount();

    /**
     * 获取今日新增用户数量
     */
    long getTodayNewUserCount();
}