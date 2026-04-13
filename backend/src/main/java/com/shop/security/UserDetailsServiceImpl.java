package com.shop.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.dao.entity.User;
import com.shop.dao.mapper.UserMapper;
import com.shop.dao.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getIsDeleted, 0)
                .and(wrapper -> wrapper
                        .eq(User::getUsername, username)
                        .or()
                        .eq(User::getEmail, username)
                        .or()
                        .eq(User::getPhone, username));

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

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

        return new SecurityUser(user, roleCodes);
    }
}
