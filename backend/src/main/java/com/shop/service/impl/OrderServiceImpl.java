package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dao.entity.Order;
import com.shop.dao.entity.OrderItem;
import com.shop.dao.entity.Product;
import com.shop.dao.entity.User;
import com.shop.dao.mapper.OrderItemMapper;
import com.shop.dao.mapper.OrderMapper;
import com.shop.dao.mapper.ProductMapper;
import com.shop.dao.mapper.UserMapper;
import com.shop.dto.request.OrderCreateRequest;
import com.shop.dto.request.OrderQueryRequest;
import com.shop.dto.request.OrderUpdateRequest;
import com.shop.dto.response.OrderDetailResponse;
import com.shop.dto.response.OrderStatisticsResponse;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.security.SecurityUtils;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random RANDOM = new Random();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Order> listOrders(OrderQueryRequest query) {
        Page<Order> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getOrderNo())) {
            wrapper.like(Order::getOrderNo, query.getOrderNo().trim());
        }
        if (query.getUserId() != null) {
            wrapper.eq(Order::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getUsername())) {
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .like(User::getUsername, query.getUsername().trim())
                            .eq(User::getIsDeleted, 0))
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            if (userIds.isEmpty()) {
                wrapper.eq(Order::getId, -1L);
            } else {
                wrapper.in(Order::getUserId, userIds);
            }
        }
        if (query.getStatus() != null) {
            wrapper.eq(Order::getStatus, query.getStatus());
        }
        if (query.getPaymentMethod() != null) {
            wrapper.eq(Order::getPaymentMethod, query.getPaymentMethod());
        }
        if (query.getMinAmount() != null) {
            wrapper.ge(Order::getActualAmount, query.getMinAmount());
        }
        if (query.getMaxAmount() != null) {
            wrapper.le(Order::getActualAmount, query.getMaxAmount());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(Order::getCreateTime, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(Order::getCreateTime, query.getEndDate().atTime(23, 59, 59));
        }

        applySort(wrapper, query);
        if (!Boolean.TRUE.equals(query.getIncludeDeleted())) {
            wrapper.eq(Order::getIsDeleted, 0);
        }
        applyOrderScope(wrapper);

        Page<Order> result = orderMapper.selectPage(page, wrapper);
        enrichOrders(result.getRecords());
        return result;
    }

    @Override
    public Page<Order> listCurrentUserOrders(Integer page, Integer size) {
        return getUserOrders(SecurityUtils.getCurrentUserId(), page, size);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long id) {
        Order order = requireAccessibleOrder(id);
        return buildOrderDetailResponse(order);
    }

    @Override
    public OrderDetailResponse getCurrentUserOrderDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1 || !Objects.equals(order.getUserId(), SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST);
        }
        return buildOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public Long createOrder(OrderCreateRequest request) {
        ensureAdminOrMerchant();
        return doCreateOrder(request, request.getUserId());
    }

    @Override
    @Transactional
    public Long createCurrentUserOrder(OrderCreateRequest request) {
        return doCreateOrder(request, SecurityUtils.getCurrentUserId());
    }

    @Override
    @Transactional
    public void updateOrder(Long id, OrderUpdateRequest request) {
        Order order = requireAccessibleOrder(id);
        if (order.getStatus() > 2) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "已发货订单不允许修改基础信息");
        }

        if (request.getShippingAddress() != null) {
            order.setShippingAddress(request.getShippingAddress());
        }
        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getDiscountAmount() != null) {
            order.setDiscountAmount(request.getDiscountAmount());
            order.setActualAmount(order.getTotalAmount().subtract(request.getDiscountAmount()).add(order.getShippingFee()));
        }
        if (request.getShippingFee() != null) {
            order.setShippingFee(request.getShippingFee());
            order.setActualAmount(order.getTotalAmount().subtract(order.getDiscountAmount()).add(request.getShippingFee()));
        }
        if (request.getRemark() != null) {
            order.setRemark(request.getRemark());
        }
        if (request.getShippingCompany() != null) {
            order.setShippingCompany(request.getShippingCompany());
        }
        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = requireAccessibleOrder(id);
        if (order.getStatus() != 4 && order.getStatus() != 5) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "只有已完成或已取消订单允许删除");
        }
        order.setIsDeleted(1);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, Integer status, String remark) {
        Order order = requireAccessibleOrder(id);
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        order.setRemark(remark);

        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case 2:
                order.setPaymentTime(now);
                break;
            case 3:
                order.setShippingTime(now);
                break;
            case 4:
                order.setReceiveTime(now);
                updateProductSales(id);
                break;
            case 5:
                order.setCancelTime(now);
                order.setCancelReason(remark);
                restoreProductStock(id);
                break;
            default:
                break;
        }

        orderMapper.updateById(order);
    }

    @Override
    public void batchUpdateOrderStatus(List<Long> ids, Integer status, String remark) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单 ID 列表不能为空");
        }
        for (Long id : ids) {
            try {
                updateOrderStatus(id, status, remark);
            } catch (BusinessException ex) {
                log.warn("Batch update order status failed, orderId={}, reason={}", id, ex.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void shipOrder(Long id, String shippingCompany, String trackingNumber) {
        Order order = requireAccessibleOrder(id);
        if (order.getStatus() != 2) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "只有待发货订单允许发货");
        }
        order.setStatus(3);
        order.setShippingTime(LocalDateTime.now());
        order.setShippingCompany(shippingCompany);
        order.setTrackingNumber(trackingNumber);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long id) {
        updateOrderStatus(id, 4, "用户确认收货");
    }

    @Override
    @Transactional
    public void cancelOrder(Long id, String cancelReason) {
        updateOrderStatus(id, 5, cancelReason);
    }

    @Override
    @Transactional
    public void cancelCurrentUserOrder(Long id, String cancelReason) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1 || !Objects.equals(order.getUserId(), SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "只有待付款订单允许取消");
        }
        updateOrderStatus(id, 5, cancelReason);
    }

    @Override
    @Transactional
    public void refundOrder(Long id, BigDecimal refundAmount, String refundReason) {
        Order order = requireAccessibleOrder(id);
        if (order.getStatus() != 4) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "只有已完成订单允许退款");
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "退款金额必须大于 0");
        }
        if (refundAmount.compareTo(order.getActualAmount()) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "退款金额不能超过实付金额");
        }

        order.setStatus(6);
        order.setRemark("退款原因: " + refundReason + ", 金额: " + refundAmount);
        orderMapper.updateById(order);
    }

    @Override
    public OrderStatisticsResponse getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getIsDeleted, 0)
                .ge(startDate != null, Order::getCreateTime, startDate.atStartOfDay())
                .le(endDate != null, Order::getCreateTime, endDate.atTime(23, 59, 59));
        applyOrderScope(wrapper);

        List<Order> orders = orderMapper.selectList(wrapper);
        OrderStatisticsResponse response = new OrderStatisticsResponse();
        response.setTotalOrders((long) orders.size());
        response.setTotalSales(orders.stream().map(Order::getActualAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        response.setAverageOrderValue(orders.isEmpty()
                ? BigDecimal.ZERO
                : response.getTotalSales().divide(BigDecimal.valueOf(orders.size()), 2, BigDecimal.ROUND_HALF_UP));
        response.setPendingPaymentCount((long) orders.stream().filter(order -> order.getStatus() == 1).count());
        response.setPendingShipmentCount((long) orders.stream().filter(order -> order.getStatus() == 2).count());
        response.setShippedCount((long) orders.stream().filter(order -> order.getStatus() == 3).count());
        response.setCompletedCount((long) orders.stream().filter(order -> order.getStatus() == 4).count());
        response.setCancelledCount((long) orders.stream().filter(order -> order.getStatus() == 5).count());
        return response;
    }

    @Override
    public Map<String, Object> getTodayOrderStatistics() {
        LocalDate today = LocalDate.now();
        return buildStatisticsMap(today.atStartOfDay(), today.atTime(23, 59, 59));
    }

    @Override
    public Page<Order> getUserOrders(Long userId, Integer page, Integer size) {
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getIsDeleted, 0)
                .orderByDesc(Order::getCreateTime));
        enrichOrders(result.getRecords());
        return result;
    }

    @Override
    public List<Order> exportOrders(OrderQueryRequest query) {
        query.setPage(1);
        query.setSize(Integer.MAX_VALUE);
        return listOrders(query).getRecords();
    }

    @Override
    public String generateOrderNo() {
        return LocalDate.now().format(ORDER_NO_FORMATTER) + String.format("%04d", RANDOM.nextInt(10000));
    }

    @Override
    public BigDecimal calculateOrderAmount(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST);
        }
        return order.getActualAmount();
    }

    @Override
    public boolean checkOrderOwnership(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        return order != null && order.getIsDeleted() == 0 && Objects.equals(order.getUserId(), userId);
    }

    @Override
    @Transactional
    public void confirmReceipt(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1 || !Objects.equals(order.getUserId(), SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != 3) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "只有已发货订单允许确认收货");
        }
        updateOrderStatus(id, 4, "用户确认收货");
    }

    private Long doCreateOrder(OrderCreateRequest request, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderCreateRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMapper.selectById(itemRequest.getProductId());
            if (product == null || product.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_EXIST, "商品不存在: " + itemRequest.getProductId());
            }
            if (product.getStatus() == 0) {
                throw new BusinessException(ErrorCode.PRODUCT_DISABLED, "商品已下架: " + product.getName());
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT, "库存不足: " + product.getName());
            }

            BigDecimal price = itemRequest.getPrice() != null ? itemRequest.getPrice() : product.getPrice();
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(itemRequest.getProductName() != null ? itemRequest.getProductName() : product.getName());
            orderItem.setProductImage(itemRequest.getProductImage() != null ? itemRequest.getProductImage() : product.getMainImage());
            orderItem.setPrice(price);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);

            product.setStock(product.getStock() - itemRequest.getQuantity());
            productMapper.updateById(product);
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        order.setActualAmount(totalAmount.subtract(order.getDiscountAmount()).add(order.getShippingFee()));
        order.setStatus(1);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setShippingAddress(request.getShippingAddress());
        order.setRemark(request.getRemark());
        order.setIsDeleted(0);
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        return order.getId();
    }

    private OrderDetailResponse buildOrderDetailResponse(Order order) {
        OrderDetailResponse response = new OrderDetailResponse();
        BeanUtils.copyProperties(order, response);
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            response.setUsername(user.getUsername());
        }
        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        response.setItems(orderItems.stream().map(item -> {
            OrderDetailResponse.OrderItemResponse result = new OrderDetailResponse.OrderItemResponse();
            BeanUtils.copyProperties(item, result);
            return result;
        }).collect(Collectors.toList()));
        return response;
    }

    private void applySort(LambdaQueryWrapper<Order> wrapper, OrderQueryRequest query) {
        if (!StringUtils.hasText(query.getSortBy())) {
            wrapper.orderByDesc(Order::getCreateTime);
            return;
        }

        boolean asc = "asc".equalsIgnoreCase(query.getSortDirection());
        switch (query.getSortBy()) {
            case "total_amount":
                wrapper.orderBy(true, asc, Order::getTotalAmount);
                break;
            case "actual_amount":
                wrapper.orderBy(true, asc, Order::getActualAmount);
                break;
            case "create_time":
            default:
                wrapper.orderBy(true, asc, Order::getCreateTime);
                break;
        }
    }

    private Map<String, Object> buildStatisticsMap(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Order::getCreateTime, start, end).eq(Order::getIsDeleted, 0);
        applyOrderScope(wrapper);

        List<Order> orders = orderMapper.selectList(wrapper);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orders.size());
        stats.put("totalSales", orders.stream().map(Order::getActualAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.put("pendingPayment", orders.stream().filter(order -> order.getStatus() == 1).count());
        stats.put("pendingShipment", orders.stream().filter(order -> order.getStatus() == 2).count());
        stats.put("shipped", orders.stream().filter(order -> order.getStatus() == 3).count());
        stats.put("completed", orders.stream().filter(order -> order.getStatus() == 4).count());
        return stats;
    }

    private void validateStatusTransition(Integer currentStatus, Integer newStatus) {
        Map<Integer, List<Integer>> allowedTransitions = new HashMap<>();
        allowedTransitions.put(1, Arrays.asList(2, 5));
        allowedTransitions.put(2, Arrays.asList(3, 5));
        allowedTransitions.put(3, Collections.singletonList(4));
        allowedTransitions.put(4, Arrays.asList(5, 6));
        allowedTransitions.put(5, Collections.emptyList());
        allowedTransitions.put(6, Collections.emptyList());

        if (Objects.equals(currentStatus, newStatus)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态未变化");
        }
        if (!allowedTransitions.getOrDefault(currentStatus, Collections.emptyList()).contains(newStatus)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "不允许的状态流转: " + currentStatus + " -> " + newStatus);
        }
    }

    private void updateProductSales(Long orderId) {
        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null && product.getIsDeleted() == 0) {
                product.setSales(product.getSales() + item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    private void restoreProductStock(Long orderId) {
        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null && product.getIsDeleted() == 0) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    private void applyOrderScope(LambdaQueryWrapper<Order> wrapper) {
        if (SecurityUtils.canViewAdminOrderScope()) {
            return;
        }
        if (SecurityUtils.isMerchant()) {
            List<Long> orderIds = findMerchantVisibleOrderIds(SecurityUtils.getCurrentUsername());
            if (!orderIds.isEmpty()) {
                wrapper.in(Order::getId, orderIds);
            } else {
                log.warn("No merchant scoped orders resolved for {}", SecurityUtils.getCurrentUsername());
            }
            return;
        }
        wrapper.eq(Order::getUserId, SecurityUtils.getCurrentUserId());
    }

    private void validateOrderAccess(Order order) {
        if (SecurityUtils.canViewAdminOrderScope()) {
            return;
        }
        if (SecurityUtils.isMerchant()) {
            if (!findMerchantVisibleOrderIds(SecurityUtils.getCurrentUsername()).contains(order.getId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该订单");
            }
            return;
        }
        if (!Objects.equals(order.getUserId(), SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该订单");
        }
    }

    private List<Long> findMerchantVisibleOrderIds(String username) {
        List<Long> productIds = productMapper.selectList(new LambdaQueryWrapper<Product>()
                        .eq(Product::getCreateBy, username)
                        .eq(Product::getIsDeleted, 0))
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getProductId, productIds))
                .stream()
                .map(OrderItem::getOrderId)
                .distinct()
                .collect(Collectors.toList());
    }

    private void ensureAdminOrMerchant() {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isMerchant()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权创建后台订单");
        }
    }

    private void enrichOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Map<Long, User> userMap = userMapper.selectBatchIds(orders.stream()
                        .map(Order::getUserId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        for (Order order : orders) {
            User user = userMap.get(order.getUserId());
            if (user != null) {
                order.setUsername(user.getUsername());
            }
            fillShippingContact(order);
        }
    }

    private void fillShippingContact(Order order) {
        if (!StringUtils.hasText(order.getShippingAddress())) {
            return;
        }
        try {
            Map<String, Object> address = OBJECT_MAPPER.readValue(order.getShippingAddress(), new TypeReference<Map<String, Object>>() {});
            if (address.get("name") != null) {
                order.setCustomerName(String.valueOf(address.get("name")));
            }
            if (address.get("phone") != null) {
                order.setCustomerPhone(String.valueOf(address.get("phone")));
            }
        } catch (Exception ex) {
            log.debug("Failed to parse shipping address for order {}", order.getId(), ex);
        }
    }

    private Order requireAccessibleOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST);
        }
        validateOrderAccess(order);
        return order;
    }
}
