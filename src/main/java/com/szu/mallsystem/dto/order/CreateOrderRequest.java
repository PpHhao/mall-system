package com.szu.mallsystem.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "addressId is required")
    private Long addressId;

    @Valid
    private List<OrderItemRequest> items;

    private String remark;

    private Integer payMethod;

    /**
     * 若 items 为空且 useCart = true，则从当前用户购物车选中的商品下单。
     */
    private Boolean useCart;
}
