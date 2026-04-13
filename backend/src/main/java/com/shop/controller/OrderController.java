package com.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Order;
import com.shop.dto.request.OrderCreateRequest;
import com.shop.dto.response.ApiResponse;
import com.shop.dto.response.OrderDetailResponse;
import com.shop.dto.response.PageResponse;
import com.shop.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "用户订单", description = "普通用户订单接口")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/my")
    public ApiResponse<PageResponse<Order>> listMyOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Order> orderPage = orderService.listCurrentUserOrders(page, size);
        return ApiResponse.success(PageResponse.of(
                orderPage.getRecords(),
                orderPage.getTotal(),
                (int) orderPage.getCurrent(),
                (int) orderPage.getSize()
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getMyOrderDetail(@PathVariable Long id) {
        return ApiResponse.success(orderService.getCurrentUserOrderDetail(id));
    }

    @PostMapping("")
    public ApiResponse<Long> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(orderService.createCurrentUserOrder(request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id, @RequestParam(required = false) String cancelReason) {
        orderService.cancelCurrentUserOrder(id, cancelReason);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/confirm-receipt")
    public ApiResponse<Void> confirmReceipt(@PathVariable Long id) {
        orderService.confirmReceipt(id);
        return ApiResponse.success(null);
    }
}
