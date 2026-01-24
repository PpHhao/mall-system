package com.szu.mallsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundVO {
    private Long id;

    private Long paymentId;

    private String payNo;

    private Long orderId;

    private String orderNo;

    private String refundNo;

    private BigDecimal amount;

    private Integer status;

    private String statusName;

    private String reason;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private String processedBy;
}
