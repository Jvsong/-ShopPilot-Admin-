package com.shop.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.dao.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关系 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户 ID 查询角色名称列表
     */
    @Select("SELECT r.name FROM role r " +
            "INNER JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.is_deleted = 0")
    List<String> selectRoleNamesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 查询角色编码列表
     */
    @Select("SELECT r.code FROM role r " +
            "INNER JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.is_deleted = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
