package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderStatsVO {
    private Long totalOrders;
    private BigDecimal totalPayAmount;
    private Long pendingCount;
    private Long paidCount;
    private Long shippedCount;
    private Long completedCount;
    private Long canceledCount;
}
