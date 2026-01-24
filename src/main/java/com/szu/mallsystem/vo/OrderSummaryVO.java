package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSummaryVO {
    private Long id;
    private String orderNo;
    private Integer status;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer itemCount;
    private LocalDateTime createdAt;
}
