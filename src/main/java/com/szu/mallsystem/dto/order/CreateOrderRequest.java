package com.szu.mallsystem.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "addressId is required")
    private Long addressId;

    @NotEmpty(message = "items is required")
    @Valid
    private List<OrderItemRequest> items;

    private String remark;

    private Integer payMethod;
}
