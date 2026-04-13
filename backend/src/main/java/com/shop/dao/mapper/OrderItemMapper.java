package com.shop.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.dao.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单商品项Mapper接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}