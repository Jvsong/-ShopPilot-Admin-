package com.shop.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.dao.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}