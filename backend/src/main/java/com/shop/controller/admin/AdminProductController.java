package com.shop.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Product;
import com.shop.dto.request.ProductCreateRequest;
import com.shop.dto.request.ProductQueryRequest;
import com.shop.dto.request.ProductUpdateRequest;
import com.shop.dto.response.ApiResponse;
import com.shop.dto.response.PageResponse;
import com.shop.dto.response.ProductDetailResponse;
import com.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 后台商品管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品相关接口")
public class AdminProductController {

    private final ProductService productService;

    @Operation(summary = "获取商品列表", description = "分页查询商品列表，支持搜索和筛选")
    @GetMapping("")
    public ApiResponse<PageResponse<Product>> listProducts(
            @Parameter(description = "商品查询参数") @Valid ProductQueryRequest request) {
        Page<Product> page = productService.listProducts(request);
        PageResponse<Product> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取商品详情", description = "根据商品ID获取商品详细信息")
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProductDetail(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id) {
        ProductDetailResponse response = productService.getProductDetail(id);
        return ApiResponse.success(response);
    }

    @Operation(summary = "创建商品", description = "创建新商品")
    @PostMapping("")
    public ApiResponse<Long> createProduct(
            @Parameter(description = "商品创建参数", required = true)
            @Valid @RequestBody ProductCreateRequest request) {
        Long productId = productService.createProduct(request);
        return ApiResponse.success(productId);
    }

    @Operation(summary = "更新商品", description = "更新商品信息")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "商品更新参数", required = true)
            @Valid @RequestBody ProductUpdateRequest request) {
        productService.updateProduct(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除商品", description = "删除商品（软删除）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新商品状态", description = "更新商品上架/下架状态")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateProductStatus(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "商品状态：0-下架，1-上架", required = true) @RequestParam Integer status) {
        productService.updateProductStatus(id, status);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量更新商品状态", description = "批量更新商品上架/下架状态")
    @PatchMapping("/batch-status")
    public ApiResponse<Void> batchUpdateProductStatus(
            @Parameter(description = "商品ID列表", required = true) @RequestBody List<Long> ids,
            @Parameter(description = "商品状态：0-下架，1-上架", required = true) @RequestParam Integer status) {
        productService.batchUpdateProductStatus(ids, status);
        return ApiResponse.success(null);
    }

    @Operation(summary = "增加商品库存", description = "增加商品库存数量")
    @PatchMapping("/{id}/increase-stock")
    public ApiResponse<Void> increaseStock(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "增加数量", required = true) @RequestParam Integer quantity) {
        productService.increaseStock(id, quantity);
        return ApiResponse.success(null);
    }

    @Operation(summary = "减少商品库存", description = "减少商品库存数量")
    @PatchMapping("/{id}/decrease-stock")
    public ApiResponse<Void> decreaseStock(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "减少数量", required = true) @RequestParam Integer quantity) {
        productService.decreaseStock(id, quantity);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取热门商品", description = "获取热销商品列表")
    @GetMapping("/hot")
    public ApiResponse<List<Product>> getHotProducts(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getHotProducts(limit);
        return ApiResponse.success(products);
    }

    @Operation(summary = "获取新品推荐", description = "获取新品推荐列表")
    @GetMapping("/new")
    public ApiResponse<List<Product>> getNewProducts(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getNewProducts(limit);
        return ApiResponse.success(products);
    }
}