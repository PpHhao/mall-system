package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Integer status;
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
    private Integer payMethod;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime canceledAt;
    private String cancelReason;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String postalCode;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemVO> items;
}
