package com.szu.mallsystem.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueryPaymentRequest {
    private Long userId;

    private Long orderId;

    private Integer status;

    @NotNull(message = "页码不能为空")
    private Integer page;

    @NotNull(message = "每页大小不能为空")
    private Integer size;
}
