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
public class PaymentVO {
    private Long id;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String payNo;

    private Integer payMethod;

    private String payMethodName;

    private BigDecimal amount;

    private Integer status;

    private String statusName;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}
