package com.shop.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.dao.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}