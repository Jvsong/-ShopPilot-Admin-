package com.shop.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品查询请求参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProductQueryRequest extends PageRequest {

    /**
     * 商品名称（模糊搜索）
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 是否热销：0-否，1-是
     */
    private Integer isHot;

    /**
     * 是否新品：0-否，1-是
     */
    private Integer isNew;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 排序字段：默认-create_time，价格-price，销量-sales
     */
    private String sortBy;

    /**
     * 排序方向：asc-升序，desc-降序
     */
    private String sortDirection;
}