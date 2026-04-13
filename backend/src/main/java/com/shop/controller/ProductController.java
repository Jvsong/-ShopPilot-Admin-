package com.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Product;
import com.shop.dto.request.ProductQueryRequest;
import com.shop.dto.response.ApiResponse;
import com.shop.dto.response.PageResponse;
import com.shop.dto.response.ProductDetailResponse;
import com.shop.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品浏览", description = "普通用户商品浏览接口")
public class ProductController {

    private final ProductService productService;

    @GetMapping("")
    public ApiResponse<PageResponse<Product>> listProducts(@Valid ProductQueryRequest request) {
        Page<Product> page = productService.listVisibleProducts(request);
        return ApiResponse.success(PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable Long id) {
        return ApiResponse.success(productService.getVisibleProductDetail(id));
    }
}
