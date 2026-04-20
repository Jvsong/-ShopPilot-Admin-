package com.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.Order;
import com.shop.dto.request.OrderCreateRequest;
import com.shop.dto.request.OrderQueryRequest;
import com.shop.dto.request.OrderUpdateRequest;
import com.shop.dto.response.OrderDetailResponse;
import com.shop.dto.response.OrderStatisticsResponse;
import com.shop.dto.response.RestockAnalysisResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 分页查询订单列表
     */
    Page<Order> listOrders(OrderQueryRequest query);

    /**
     * 获取当前登录用户订单列表
     */
    Page<Order> listCurrentUserOrders(Integer page, Integer size);

    /**
     * 获取订单详情
     */
    OrderDetailResponse getOrderDetail(Long id);

    /**
     * 获取当前登录用户订单详情
     */
    OrderDetailResponse getCurrentUserOrderDetail(Long id);

    /**
     * 创建订单
     */
    Long createOrder(OrderCreateRequest request);

    /**
     * 当前登录用户下单
     */
    Long createCurrentUserOrder(OrderCreateRequest request);

    /**
     * 更新订单信息
     */
    void updateOrder(Long id, OrderUpdateRequest request);

    /**
     * 删除订单（软删除）
     */
    void deleteOrder(Long id);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long id, Integer status, String remark);

    /**
     * 批量更新订单状态
     */
    void batchUpdateOrderStatus(List<Long> ids, Integer status, String remark);

    /**
     * 订单发货
     */
    void shipOrder(Long id, String shippingCompany, String trackingNumber);

    /**
     * 订单完成（确认收货）
     */
    void completeOrder(Long id);

    /**
     * 订单取消
     */
    void cancelOrder(Long id, String cancelReason);

    /**
     * 当前登录用户取消本人订单
     */
    void cancelCurrentUserOrder(Long id, String cancelReason);

    /**
     * 订单退款
     */
    void refundOrder(Long id, BigDecimal refundAmount, String refundReason);

    /**
     * 获取订单统计信息
     */
    OrderStatisticsResponse getOrderStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取补货分析
     */
    RestockAnalysisResponse getRestockAnalysis(LocalDate startDate, LocalDate endDate);

    /**
     * 获取今日订单统计
     */
    Map<String, Object> getTodayOrderStatistics();

    /**
     * 获取用户订单列表
     */
    Page<Order> getUserOrders(Long userId, Integer page, Integer size);

    /**
     * 导出订单数据
     */
    List<Order> exportOrders(OrderQueryRequest query);

    /**
     * 生成订单号
     */
    String generateOrderNo();

    /**
     * 计算订单金额
     */
    BigDecimal calculateOrderAmount(Long orderId);

    /**
     * 检查订单是否存在且属于指定用户
     */
    boolean checkOrderOwnership(Long orderId, Long userId);

    /**
     * 当前登录用户确认收货
     */
    void confirmReceipt(Long id);
}
