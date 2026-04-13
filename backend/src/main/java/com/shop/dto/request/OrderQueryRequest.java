package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单查询请求")
public class OrderQueryRequest extends PageRequest {

    @Schema(description = "订单号（精确查询）", example = "202604050001")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名（模糊查询）", example = "zhang")
    private String username;

    @Schema(description = "订单状态: 1-待付款, 2-待发货, 3-已发货, 4-已完成, 5-已取消", example = "1")
    private Integer status;

    @Schema(description = "支付方式: 1-支付宝, 2-微信, 3-银行卡", example = "1")
    private Integer paymentMethod;

    @Schema(description = "最小金额", example = "0.00")
    private BigDecimal minAmount;

    @Schema(description = "最大金额", example = "1000.00")
    private BigDecimal maxAmount;

    @Schema(description = "开始日期", example = "2026-04-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2026-04-30")
    private LocalDate endDate;

    @Schema(description = "排序字段: create_time, total_amount, actual_amount", example = "create_time")
    private String sortBy;

    @Schema(description = "排序方向: asc, desc", example = "desc")
    private String sortDirection;

    @Schema(description = "是否包含已删除订单", example = "false")
    private Boolean includeDeleted = false;
}