package com.shop.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.dao.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}