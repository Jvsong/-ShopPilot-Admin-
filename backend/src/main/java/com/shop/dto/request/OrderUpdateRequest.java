package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单更新请求
 */
@Data
@Schema(description = "订单更新请求")
public class OrderUpdateRequest {

    @Schema(description = "收货地址信息（JSON格式）")
    private String shippingAddress;

    @Schema(description = "支付方式: 1-支付宝, 2-微信, 3-银行卡", example = "1")
    private Integer paymentMethod;

    @Schema(description = "优惠金额", example = "10.00")
    private BigDecimal discountAmount;

    @Schema(description = "运费", example = "0.00")
    private BigDecimal shippingFee;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "发货公司")
    private String shippingCompany;

    @Schema(description = "物流单号")
    private String trackingNumber;
}