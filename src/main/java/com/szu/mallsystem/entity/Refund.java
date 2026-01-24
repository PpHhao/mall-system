package com.szu.mallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refunds")
public class Refund {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paymentId;

    private Long orderId;

    @TableField("refund_no")
    private String refundNo;

    private BigDecimal amount;

    private Integer status;

    private String reason;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("processed_at")
    private LocalDateTime processedAt;

    @TableField("processed_by")
    private Long processedBy;
}
