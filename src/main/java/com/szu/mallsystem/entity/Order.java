package com.szu.mallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    private Integer status;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("freight_amount")
    private BigDecimal freightAmount;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("pay_method")
    private Integer payMethod;

    @TableField("paid_at")
    private LocalDateTime paidAt;

    @TableField("shipped_at")
    private LocalDateTime shippedAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;

    @TableField("canceled_at")
    private LocalDateTime canceledAt;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("receiver_name")
    private String receiverName;

    @TableField("receiver_phone")
    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    private String detail;

    @TableField("postal_code")
    private String postalCode;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
