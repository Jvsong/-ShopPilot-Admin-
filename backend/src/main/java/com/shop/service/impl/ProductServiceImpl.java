package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Category;
import com.shop.dao.entity.Product;
import com.shop.dao.entity.ProductAttribute;
import com.shop.dao.mapper.CategoryMapper;
import com.shop.dao.mapper.ProductAttributeMapper;
import com.shop.dao.mapper.ProductMapper;
import com.shop.dto.request.ProductAttributeRequest;
import com.shop.dto.request.ProductCreateRequest;
import com.shop.dto.request.ProductQueryRequest;
import com.shop.dto.request.ProductUpdateRequest;
import com.shop.dto.response.ProductAttributeResponse;
import com.shop.dto.response.ProductDetailResponse;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.security.SecurityUtils;
import com.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.shop.util.QueryBuilder;
import com.shop.util.ValidationUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductAttributeMapper productAttributeMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<Product> listProducts(ProductQueryRequest query) {
        return listProductsByScope(query, false);
    }

    @Override
    public Page<Product> listVisibleProducts(ProductQueryRequest query) {
        return listProductsByScope(query, true);
    }

    private Page<Product> listProductsByScope(ProductQueryRequest query, boolean publicOnly) {
        Page<Product> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 使用QueryBuilder构建查询条件
        QueryBuilder.Builder.of(wrapper)
                .likeIfPresent(query.getName(), Product::getName)
                .eqIfNotNull(query.getStatus(), Product::getStatus)
                .eqIfNotNull(query.getIsHot(), Product::getIsHot)
                .eqIfNotNull(query.getIsNew(), Product::getIsNew)
                .geIfNotNull(query.getMinPrice(), Product::getPrice)
                .leIfNotNull(query.getMaxPrice(), Product::getPrice)
                .notDeleted(Product::getIsDeleted);

        // 分类筛选处理：支持按父分类查询所有子分类商品
        if (query.getCategoryId() != null) {
            List<Long> categoryIds = getCategoryIdsIncludingChildren(query.getCategoryId());
            if (!categoryIds.isEmpty()) {
                wrapper.in(Product::getCategoryId, categoryIds);
            } else {
                // 如果分类ID列表为空（例如分类不存在或已删除），则返回空结果
                wrapper.eq(Product::getCategoryId, -1);
            }
        }

        applyProductScope(wrapper, publicOnly);

        // 排序处理
        if (StringUtils.hasText(query.getSortBy())) {
            String sortBy = query.getSortBy();
            boolean asc = "asc".equalsIgnoreCase(query.getSortDirection());
            switch (sortBy) {
                case "price":
                    wrapper.orderBy(true, asc, Product::getPrice);
                    break;
                case "sales":
                    wrapper.orderBy(true, asc, Product::getSales);
                    break;
                case "create_time":
                default:
                    wrapper.orderBy(true, asc, Product::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }

        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public ProductDetailResponse getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        validateManageProductAccess(product);
        return buildProductDetailResponse(product);
    }

    @Override
    public ProductDetailResponse getVisibleProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1 || product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return buildProductDetailResponse(product);
    }

    private ProductDetailResponse buildProductDetailResponse(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        BeanUtils.copyProperties(product, response);

        // 获取商品属性
        LambdaQueryWrapper<ProductAttribute> attributeWrapper = new LambdaQueryWrapper<>();
        attributeWrapper.eq(ProductAttribute::getProductId, product.getId())
                .eq(ProductAttribute::getIsDeleted, 0)
                .orderByAsc(ProductAttribute::getSortOrder);
        List<ProductAttribute> attributes = productAttributeMapper.selectList(attributeWrapper);

        // 转换属性响应
        List<ProductAttributeResponse> attributeResponses = attributes.stream()
                .map(attr -> {
                    ProductAttributeResponse attrResp = new ProductAttributeResponse();
                    BeanUtils.copyProperties(attr, attrResp);
                    // 映射字段名
                    attrResp.setAttributeName(attr.getAttributeName());
                    attrResp.setAttributeValue(attr.getAttributeValue());
                    attrResp.setSortOrder(attr.getSortOrder());
                    return attrResp;
                })
                .collect(Collectors.toList());
        response.setAttributes(attributeResponses);

        return response;
    }

    @Override
    @Transactional
    public Long createProduct(ProductCreateRequest request) {
        ensureCanManageProducts();

        // 检查商品名称是否重复
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getName, request.getName())
                .eq(Product::getIsDeleted, 0);
        Long count = productMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PRODUCT_NAME_EXISTS);
        }

        // 创建商品
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        // 处理图片列表（转为JSON字符串）
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // 简单处理：使用逗号分隔，实际项目中可以使用JSON序列化
            product.setImages(String.join(",", request.getImages()));
        }
        product.setSales(0); // 初始销量为0
        product.setIsDeleted(0);
        productMapper.insert(product);

        // 保存商品属性
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            for (ProductAttributeRequest attrRequest : request.getAttributes()) {
                ProductAttribute attribute = new ProductAttribute();
                attribute.setProductId(product.getId());
                attribute.setAttributeName(attrRequest.getAttributeName());
                attribute.setAttributeValue(attrRequest.getAttributeValue());
                attribute.setSortOrder(attrRequest.getSortOrder());
                attribute.setIsDeleted(0);
                productAttributeMapper.insert(attribute);
            }
        }

        return product.getId();
    }

    @Override
    @Transactional
    public void updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        validateManageProductAccess(product);

        // 如果修改了商品名称，检查是否重复
        if (StringUtils.hasText(request.getName()) && !request.getName().equals(product.getName())) {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getName, request.getName())
                    .ne(Product::getId, id)
                    .eq(Product::getIsDeleted, 0);
            Long count = productMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PRODUCT_NAME_EXISTS);
            }
        }

        // 更新商品信息
        BeanUtils.copyProperties(request, product, "id", "isDeleted", "createTime", "createBy");
        if (request.getImages() != null) {
            product.setImages(String.join(",", request.getImages()));
        }
        productMapper.updateById(product);

        // 更新商品属性（先删除旧属性，再添加新属性）
        if (request.getAttributes() != null) {
            // 软删除旧属性
            LambdaQueryWrapper<ProductAttribute> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ProductAttribute::getProductId, id)
                    .eq(ProductAttribute::getIsDeleted, 0);
            List<ProductAttribute> oldAttributes = productAttributeMapper.selectList(deleteWrapper);
            for (ProductAttribute attr : oldAttributes) {
                attr.setIsDeleted(1);
                productAttributeMapper.updateById(attr);
            }

            // 添加新属性
            for (ProductAttributeRequest attrRequest : request.getAttributes()) {
                ProductAttribute attribute = new ProductAttribute();
                attribute.setProductId(id);
                attribute.setAttributeName(attrRequest.getAttributeName());
                attribute.setAttributeValue(attrRequest.getAttributeValue());
                attribute.setSortOrder(attrRequest.getSortOrder());
                attribute.setIsDeleted(0);
                productAttributeMapper.insert(attribute);
            }
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        validateManageProductAccess(product);

        // 软删除商品
        product.setIsDeleted(1);
        productMapper.updateById(product);

        // 软删除商品属性
        LambdaQueryWrapper<ProductAttribute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductAttribute::getProductId, id)
                .eq(ProductAttribute::getIsDeleted, 0);
        List<ProductAttribute> attributes = productAttributeMapper.selectList(wrapper);
        for (ProductAttribute attr : attributes) {
            attr.setIsDeleted(1);
            productAttributeMapper.updateById(attr);
        }
    }

    @Override
    public void updateProductStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        validateManageProductAccess(product);

        ValidationUtils.validateStatus(status, "商品状态值不合法");

        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Override
    public void batchUpdateProductStatus(List<Long> ids, Integer status) {
        ValidationUtils.requireNonEmpty(ids, "商品ID列表不能为空");
        ValidationUtils.validateStatus(status, "商品状态值不合法");

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getId, ids)
                .eq(Product::getIsDeleted, 0);

        List<Product> products = productMapper.selectList(wrapper);
        for (Product product : products) {
            validateManageProductAccess(product);
            product.setStatus(status);
            productMapper.updateById(product);
        }
    }

    @Override
    public void increaseStock(Long id, Integer quantity) {
        ValidationUtils.requirePositive(quantity, "增加数量必须大于0");

        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        validateManageProductAccess(product);

        product.setStock(product.getStock() + quantity);
        productMapper.updateById(product);
    }

    @Override
    public void decreaseStock(Long id, Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "减少数量必须大于0");
        }

        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        validateManageProductAccess(product);

        if (product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }

        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);
    }

    @Override
    public List<Product> getHotProducts(int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .eq(Product::getIsHot, 1)
                .eq(Product::getIsDeleted, 0)
                .orderByDesc(Product::getSales)
                .last("LIMIT " + limit);
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> getNewProducts(int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .eq(Product::getIsNew, 1)
                .eq(Product::getIsDeleted, 0)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit);
        return productMapper.selectList(wrapper);
    }

    private void applyProductScope(LambdaQueryWrapper<Product> wrapper, boolean publicOnly) {
        if (publicOnly) {
            wrapper.eq(Product::getStatus, 1);
            return;
        }

        if (SecurityUtils.isMerchant()) {
            wrapper.eq(Product::getCreateBy, SecurityUtils.getCurrentUsername());
        }
    }

    private void validateManageProductAccess(Product product) {
        ensureCanManageProducts();
        if (SecurityUtils.isAdmin()) {
            return;
        }

        if (SecurityUtils.isMerchant() && SecurityUtils.getCurrentUsername().equals(product.getCreateBy())) {
            return;
        }

        throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该商品");
    }

    private List<Long> getCategoryIdsIncludingChildren(Long categoryId) {
        // 如果categoryId为null，返回空列表
        if (categoryId == null) {
            return Collections.emptyList();
        }

        // 查询所有子分类（包括自身）
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Category::getId);
        wrapper.and(w -> w.eq(Category::getId, categoryId).or().eq(Category::getParentId, categoryId));
        wrapper.eq(Category::getStatus, 1);
        wrapper.eq(Category::getIsDeleted, 0);

        List<Category> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(Category::getId).collect(Collectors.toList());
    }

    private void ensureCanManageProducts() {
        if (SecurityUtils.isAdmin() || SecurityUtils.isMerchant()) {
            return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "无权管理商品");
    }
}
