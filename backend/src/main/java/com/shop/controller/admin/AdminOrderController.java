package com.shop.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Order;
import com.shop.dto.request.OrderCreateRequest;
import com.shop.dto.request.OrderQueryRequest;
import com.shop.dto.request.OrderUpdateRequest;
import com.shop.dto.response.ApiResponse;
import com.shop.dto.response.OrderDetailResponse;
import com.shop.dto.response.OrderStatisticsResponse;
import com.shop.dto.response.PageResponse;
import com.shop.dto.response.RestockAnalysisResponse;
import com.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 后台订单管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单相关接口")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "获取订单列表", description = "分页查询订单列表，支持搜索和筛选")
    @GetMapping("")
    public ApiResponse<PageResponse<Order>> listOrders(
            @Parameter(description = "订单查询参数") @Valid OrderQueryRequest request) {
        Page<Order> page = orderService.listOrders(request);
        PageResponse<Order> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        OrderDetailResponse response = orderService.getOrderDetail(id);
        return ApiResponse.success(response);
    }

    @Operation(summary = "创建订单", description = "创建新订单（后台手动创建）")
    @PostMapping("")
    public ApiResponse<Long> createOrder(
            @Parameter(description = "订单创建参数", required = true)
            @Valid @RequestBody OrderCreateRequest request) {
        Long orderId = orderService.createOrder(request);
        return ApiResponse.success(orderId);
    }

    @Operation(summary = "更新订单信息", description = "更新订单基本信息")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Parameter(description = "订单更新参数", required = true)
            @Valid @RequestBody OrderUpdateRequest request) {
        orderService.updateOrder(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除订单", description = "删除订单（软删除）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新订单状态", description = "更新订单状态")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateOrderStatus(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Parameter(description = "订单状态：1-待付款, 2-待发货, 3-已发货, 4-已完成, 5-已取消", required = true)
            @RequestParam Integer status,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        orderService.updateOrderStatus(id, status, remark);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量更新订单状态", description = "批量更新订单状态")
    @PostMapping("/batch-status")
    public ApiResponse<Void> batchUpdateOrderStatus(
            @Parameter(description = "订单ID列表", required = true) @RequestBody List<Long> ids,
            @Parameter(description = "订单状态", required = true) @RequestParam Integer status,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        orderService.batchUpdateOrderStatus(ids, status, remark);
        return ApiResponse.success(null);
    }

    @Operation(summary = "订单发货", description = "订单发货操作")
    @PostMapping("/{id}/ship")
    public ApiResponse<Void> shipOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Parameter(description = "发货公司", required = true) @RequestParam String shippingCompany,
            @Parameter(description = "物流单号", required = true) @RequestParam String trackingNumber) {
        orderService.shipOrder(id, shippingCompany, trackingNumber);
        return ApiResponse.success(null);
    }

    @Operation(summary = "订单完成", description = "订单完成（确认收货）")
    @PostMapping("/{id}/complete")
    public ApiResponse<Void> completeOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        orderService.completeOrder(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "订单取消", description = "取消订单")
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam(required = false) String cancelReason) {
        orderService.cancelOrder(id, cancelReason);
        return ApiResponse.success(null);
    }

    @Operation(summary = "订单退款", description = "订单退款处理")
    @PostMapping("/{id}/refund")
    public ApiResponse<Void> refundOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Parameter(description = "退款金额", required = true) @RequestParam BigDecimal refundAmount,
            @Parameter(description = "退款原因", required = true) @RequestParam String refundReason) {
        orderService.refundOrder(id, refundAmount, refundReason);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取订单统计信息", description = "获取订单统计信息")
    @GetMapping("/statistics")
    public ApiResponse<OrderStatisticsResponse> getOrderStatistics(
            @Parameter(description = "开始日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        OrderStatisticsResponse response = orderService.getOrderStatistics(startDate, endDate);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取今日订单统计", description = "获取今日订单统计数据")
    @GetMapping("/today-statistics")
    public ApiResponse<Object> getTodayOrderStatistics() {
        Map<String, Object> stats = orderService.getTodayOrderStatistics();
        return ApiResponse.success(stats);
    }

    @Operation(summary = "获取补货分析", description = "基于销量与库存生成补货分析结果")
    @GetMapping("/restock-analysis")
    public ApiResponse<RestockAnalysisResponse> getRestockAnalysis(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        RestockAnalysisResponse response = orderService.getRestockAnalysis(startDate, endDate);
        return ApiResponse.success(response);
    }

    @Operation(summary = "导出订单数据", description = "导出订单数据，支持搜索筛选")
    @GetMapping("/export")
    public ApiResponse<List<Order>> exportOrders(
            @Parameter(description = "订单查询参数") @Valid OrderQueryRequest request) {
        List<Order> orders = orderService.exportOrders(request);
        return ApiResponse.success(orders);
    }

    @Operation(summary = "获取用户订单列表", description = "获取指定用户的订单列表")
    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<Order>> getUserOrders(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") Integer size) {
        Page<Order> pageObj = orderService.getUserOrders(userId, page, size);
        PageResponse<Order> response = PageResponse.of(
                pageObj.getRecords(),
                pageObj.getTotal(),
                (int) pageObj.getCurrent(),
                (int) pageObj.getSize()
        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "计算订单金额", description = "计算订单实际支付金额")
    @GetMapping("/{id}/amount")
    public ApiResponse<BigDecimal> calculateOrderAmount(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        BigDecimal amount = orderService.calculateOrderAmount(id);
        return ApiResponse.success(amount);
    }

    @Operation(summary = "检查订单归属", description = "检查订单是否属于指定用户")
    @GetMapping("/{id}/ownership/{userId}")
    public ApiResponse<Boolean> checkOrderOwnership(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        boolean isOwner = orderService.checkOrderOwnership(id, userId);
        return ApiResponse.success(isOwner);
    }

    @Operation(summary = "生成订单号", description = "生成新的订单号")
    @GetMapping("/generate-order-no")
    public ApiResponse<String> generateOrderNo() {
        String orderNo = orderService.generateOrderNo();
        return ApiResponse.success(orderNo);
    }
}
