package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建请求
 */
@Data
@Schema(description = "订单创建请求")
public class OrderCreateRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "1")
    private Long userId;

    @NotEmpty(message = "订单商品不能为空")
    @Schema(description = "订单商品列表", required = true)
    private List<OrderItemRequest> items;

    @Schema(description = "收货地址ID", example = "1")
    private Long addressId;

    @Schema(description = "收货地址信息（JSON格式）")
    private String shippingAddress;

    @Schema(description = "支付方式: 1-支付宝, 2-微信, 3-银行卡", example = "1")
    private Integer paymentMethod;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "优惠金额", example = "10.00")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Schema(description = "运费", example = "0.00")
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Schema(description = "订单备注")
    private String remark;

    /**
     * 订单商品项请求
     */
    @Data
    @Schema(description = "订单商品项")
    public static class OrderItemRequest {

        @NotNull(message = "商品ID不能为空")
        @Schema(description = "商品ID", required = true, example = "1")
        private Long productId;

        @NotNull(message = "商品数量不能为空")
        @Min(value = 1, message = "商品数量必须大于0")
        @Schema(description = "商品数量", required = true, example = "2")
        private Integer quantity;

        @Schema(description = "商品单价（可选，默认从商品信息获取）", example = "99.99")
        private BigDecimal price;

        @Schema(description = "商品名称（可选，默认从商品信息获取）", example = "商品名称")
        private String productName;

        @Schema(description = "商品图片（可选，默认从商品信息获取）")
        private String productImage;
    }
}