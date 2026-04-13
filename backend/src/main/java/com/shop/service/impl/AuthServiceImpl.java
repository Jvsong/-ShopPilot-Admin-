package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.dao.entity.User;
import com.shop.dao.mapper.UserRoleMapper;
import com.shop.dao.mapper.UserMapper;
import com.shop.dto.request.LoginRequest;
import com.shop.dto.request.RegisterRequest;
import com.shop.dto.response.LoginResponse;
import com.shop.dto.response.UserInfoResponse;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.security.JwtTokenUtil;
import com.shop.security.SecurityUser;
import com.shop.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 首先检查用户状态和锁定状态
        User user = getUserByIdentifier(request.getUsername());
        if (user != null) {
            checkUserStatus(user);
        }

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 生成token
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            String accessToken = jwtTokenUtil.generateAccessToken(securityUser);
            String refreshToken = jwtTokenUtil.generateRefreshToken(securityUser);

            // 更新用户登录信息
            user = securityUser.getUser();
            user.setLastLoginTime(LocalDateTime.now());
            user.setLoginFailCount(0);
            user.setLockTime(null);
            userMapper.updateById(user);

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(jwtTokenUtil.getExpiration());
            response.setUserInfo(toUserInfoResponse(user));

            return response;

        } catch (BadCredentialsException e) {
            // 登录失败，记录失败次数
            handleLoginFailure(request.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户名或密码错误");
        }
    }

    @Override
    public void logout() {
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
        log.info("用户注销成功");
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证refreshToken
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "刷新令牌不能为空");
        }

        try {
            // 从refreshToken中提取用户名
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

            // 检查refreshToken是否过期
            if (jwtTokenUtil.isTokenExpired(refreshToken)) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            }

            // 加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!(userDetails instanceof SecurityUser)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }

            SecurityUser securityUser = (SecurityUser) userDetails;
            User user = securityUser.getUser();

            // 检查用户状态
            if (user.getStatus() != 1) {
                throw new BusinessException(ErrorCode.USER_DISABLED);
            }

            // 生成新的token
            String newAccessToken = jwtTokenUtil.generateAccessToken(securityUser);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(securityUser);

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(jwtTokenUtil.getExpiration());
            response.setUserInfo(toUserInfoResponse(user));

            return response;

        } catch (io.jsonwebtoken.JwtException e) {
            log.error("刷新令牌无效: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "刷新令牌无效");
        }
    }

    @Override
    public UserInfoResponse getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return toUserInfoResponse(securityUser.getUser());
    }

    /**
     * 根据用户名、邮箱或手机号查找用户
     */
    private User getUserByIdentifier(String identifier) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, identifier)
                   .or()
                   .eq(User::getEmail, identifier)
                   .or()
                   .eq(User::getPhone, identifier)
                   .eq(User::getIsDeleted, 0);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 检查用户状态
     */
    private void checkUserStatus(User user) {
        if (user == null) {
            return;
        }

        // 检查用户是否被禁用
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 检查用户是否被锁定
        if (user.getLockTime() != null) {
            LocalDateTime lockTime = user.getLockTime();
            LocalDateTime unlockTime = lockTime.plusMinutes(30); // 锁定30分钟

            if (LocalDateTime.now().isBefore(unlockTime)) {
                throw new BusinessException(ErrorCode.USER_LOCKED, "账号已被锁定，请在30分钟后重试");
            } else {
                // 锁定时间已过，重置锁定状态
                user.setLockTime(null);
                user.setLoginFailCount(0);
                userMapper.updateById(user);
            }
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(String username) {
        User user = getUserByIdentifier(username);
        if (user != null) {
            int failCount = user.getLoginFailCount() + 1;
            user.setLoginFailCount(failCount);

            // 如果失败次数超过5次，锁定账号30分钟
            if (failCount >= 5) {
                user.setLockTime(LocalDateTime.now());
                log.warn("用户 {} 因登录失败次数过多被锁定", username);
            }

            userMapper.updateById(user);
        }
    }

    /**
     * 转换为用户信息响应
     */
    private UserInfoResponse toUserInfoResponse(User user) {
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        List<String> roleCodes = new ArrayList<>(userRoleMapper.selectRoleCodesByUserId(user.getId()));
        if (roleCodes.isEmpty()) {
            if (user.getUserType() != null && user.getUserType() == 2) {
                roleCodes.add("ROLE_ADMIN");
            } else if (user.getUserType() != null && user.getUserType() == 3) {
                roleCodes.add("ROLE_MERCHANT");
            } else {
                roleCodes.add("ROLE_USER");
            }
        }
        response.setRoles(roleCodes);
        return response;
    }

    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.existsUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(request.getEmail()) && userMapper.existsEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱已存在");
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(request.getPhone()) && userMapper.existsPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "手机号已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setUserType(1); // 注册用户默认为普通用户
        user.setStatus(1); // 默认启用
        user.setIsDeleted(0);
        user.setLoginFailCount(0);
        user.setCreateBy("system"); // 注册用户创建者为system

        userMapper.insert(user);
        log.info("用户注册成功: {}", request.getUsername());
        return user.getId();
    }

    @Override
    @Transactional
    public void resetPassword(String username, String oldPassword, String newPassword) {
        User user = getUserByIdentifier(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);
        log.info("用户密码重置成功: {}", username);
    }

    @Override
    public void sendResetPasswordCode(String email) {
        // TODO: 发送验证码到邮箱（需要集成邮件服务）
        // 这里实现一个简单的模拟版本
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱不能为空");
        }

        User user = getUserByIdentifier(email);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        // 生成6位验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // TODO: 实际应该保存到Redis或数据库，设置过期时间
        // TODO: 发送邮件

        log.info("发送密码重置验证码到邮箱 {}: {}", email, code);
        // 模拟发送成功
    }

    @Override
    @Transactional
    public void resetPasswordByCode(String email, String code, String newPassword) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱不能为空");
        }

        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码不能为空");
        }

        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能为空");
        }

        User user = getUserByIdentifier(email);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        // TODO: 实际应该从Redis或数据库验证验证码
        // 这里简单模拟验证码正确
        if (!"123456".equals(code)) { // 临时固定验证码，实际应该从存储中获取
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码错误");
        }

        // 重置密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);
        log.info("通过验证码重置密码成功: {}", email);
    }
}
