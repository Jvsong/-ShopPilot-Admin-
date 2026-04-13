package com.shop.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品更新请求参数
 */
@Data
public class ProductUpdateRequest {

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存数量
     */
    private Integer stock;

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
     * 主图URL
     */
    private String mainImage;

    /**
     * 商品图片列表（JSON数组）
     */
    private List<String> images;

    /**
     * 商品属性列表
     */
    private List<ProductAttributeRequest> attributes;
}