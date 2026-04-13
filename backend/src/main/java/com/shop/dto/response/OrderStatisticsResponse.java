package com.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单统计响应
 */
@Data
@Schema(description = "订单统计响应")
public class OrderStatisticsResponse {

    @Schema(description = "总订单数", example = "100")
    private Long totalOrders;

    @Schema(description = "总销售额", example = "9999.99")
    private BigDecimal totalSales;

    @Schema(description = "平均客单价", example = "99.99")
    private BigDecimal averageOrderValue;

    @Schema(description = "待付款订单数", example = "10")
    private Long pendingPaymentCount;

    @Schema(description = "待发货订单数", example = "15")
    private Long pendingShipmentCount;

    @Schema(description = "已发货订单数", example = "20")
    private Long shippedCount;

    @Schema(description = "已完成订单数", example = "50")
    private Long completedCount;

    @Schema(description = "已取消订单数", example = "5")
    private Long cancelledCount;

    @Schema(description = "每日订单统计")
    private List<DailyStatistic> dailyStatistics;

    @Schema(description = "状态分布")
    private Map<Integer, Long> statusDistribution;

    @Schema(description = "支付方式分布")
    private Map<Integer, Long> paymentMethodDistribution;

    @Schema(description = "热门商品统计")
    private List<ProductStatistic> topProducts;

    /**
     * 每日统计
     */
    @Data
    @Schema(description = "每日统计")
    public static class DailyStatistic {

        @Schema(description = "日期", example = "2026-04-01")
        private String date;

        @Schema(description = "订单数", example = "10")
        private Long orderCount;

        @Schema(description = "销售额", example = "999.99")
        private BigDecimal salesAmount;
    }

    /**
     * 商品统计
     */
    @Data
    @Schema(description = "商品统计")
    public static class ProductStatistic {

        @Schema(description = "商品ID", example = "1")
        private Long productId;

        @Schema(description = "商品名称", example = "商品名称")
        private String productName;

        @Schema(description = "销售数量", example = "100")
        private Integer salesQuantity;

        @Schema(description = "销售金额", example = "9999.99")
        private BigDecimal salesAmount;
    }
}