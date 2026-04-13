package com.shop.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String customerPhone;

    @TableField(exist = false)
    private List<OrderItem> items;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 优惠金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 运费
     */
    @TableField("shipping_fee")
    private BigDecimal shippingFee;

    /**
     * 实际支付金额
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    /**
     * 订单状态: 1-待付款, 2-待发货, 3-已发货, 4-已完成, 5-已取消
     */
    private Integer status;

    /**
     * 支付方式: 1-支付宝, 2-微信, 3-银行卡
     */
    @TableField("payment_method")
    private Integer paymentMethod;

    /**
     * 支付时间
     */
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     */
    @TableField("shipping_time")
    private LocalDateTime shippingTime;

    /**
     * 物流公司
     */
    @TableField("shipping_company")
    private String shippingCompany;

    /**
     * 物流单号
     */
    @TableField("tracking_number")
    private String trackingNumber;

    /**
     * 收货时间
     */
    @TableField("receive_time")
    private LocalDateTime receiveTime;

    /**
     * 取消时间
     */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 收货地址(JSON)
     */
    @TableField("shipping_address")
    private String shippingAddress;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 是否删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
