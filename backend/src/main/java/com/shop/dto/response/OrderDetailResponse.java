package com.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情响应
 */
@Data
@Schema(description = "订单详情响应")
public class OrderDetailResponse {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单号", example = "202604050001")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "订单总金额", example = "199.98")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额", example = "10.00")
    private BigDecimal discountAmount;

    @Schema(description = "运费", example = "0.00")
    private BigDecimal shippingFee;

    @Schema(description = "实际支付金额", example = "189.98")
    private BigDecimal actualAmount;

    @Schema(description = "订单状态: 1-待付款, 2-待发货, 3-已发货, 4-已完成, 5-已取消", example = "1")
    private Integer status;

    @Schema(description = "支付方式: 1-支付宝, 2-微信, 3-银行卡", example = "1")
    private Integer paymentMethod;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "发货时间")
    private LocalDateTime shippingTime;

    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "收货地址（JSON格式）")
    private String shippingAddress;

    @Schema(description = "发货公司")
    private String shippingCompany;

    @Schema(description = "物流单号")
    private String trackingNumber;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "订单商品列表")
    private List<OrderItemResponse> items;

    /**
     * 订单商品项响应
     */
    @Data
    @Schema(description = "订单商品项")
    public static class OrderItemResponse {

        @Schema(description = "商品ID", example = "1")
        private Long productId;

        @Schema(description = "商品名称", example = "商品名称")
        private String productName;

        @Schema(description = "商品图片")
        private String productImage;

        @Schema(description = "商品单价", example = "99.99")
        private BigDecimal price;

        @Schema(description = "商品数量", example = "2")
        private Integer quantity;

        @Schema(description = "商品小计", example = "199.98")
        private BigDecimal totalPrice;
    }
}