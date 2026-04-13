package com.shop.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM user WHERE username = #{username} AND is_deleted = 0")
    boolean existsUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM user WHERE email = #{email} AND is_deleted = 0")
    boolean existsEmail(@Param("email") String email);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(*) FROM user WHERE phone = #{phone} AND is_deleted = 0")
    boolean existsPhone(@Param("phone") String phone);
}