package com.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Product;
import com.shop.dto.request.ProductQueryRequest;
import com.shop.dto.request.ProductCreateRequest;
import com.shop.dto.request.ProductUpdateRequest;
import com.shop.dto.response.ProductDetailResponse;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 分页查询商品列表
     */
    Page<Product> listProducts(ProductQueryRequest query);

    /**
     * 分页查询前台可见商品
     */
    Page<Product> listVisibleProducts(ProductQueryRequest query);

    /**
     * 获取商品详情
     */
    ProductDetailResponse getProductDetail(Long id);

    /**
     * 获取前台可见商品详情
     */
    ProductDetailResponse getVisibleProductDetail(Long id);

    /**
     * 创建商品
     */
    Long createProduct(ProductCreateRequest request);

    /**
     * 更新商品
     */
    void updateProduct(Long id, ProductUpdateRequest request);

    /**
     * 删除商品（软删除）
     */
    void deleteProduct(Long id);

    /**
     * 更新商品状态
     */
    void updateProductStatus(Long id, Integer status);

    /**
     * 批量更新商品状态
     */
    void batchUpdateProductStatus(List<Long> ids, Integer status);

    /**
     * 增加商品库存
     */
    void increaseStock(Long id, Integer quantity);

    /**
     * 减少商品库存
     */
    void decreaseStock(Long id, Integer quantity);

    /**
     * 获取热门商品
     */
    List<Product> getHotProducts(int limit);

    /**
     * 获取新品推荐
     */
    List<Product> getNewProducts(int limit);
}
