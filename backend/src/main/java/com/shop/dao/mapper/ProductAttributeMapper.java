package com.shop.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.dao.entity.ProductAttribute;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性Mapper接口
 */
@Mapper
public interface ProductAttributeMapper extends BaseMapper<ProductAttribute> {
}