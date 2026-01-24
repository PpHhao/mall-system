package com.szu.mallsystem.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayOrderRequest {
    @NotNull(message = "payMethod is required")
    @Min(value = 1, message = "payMethod must be between 1 and 3")
    @Max(value = 3, message = "payMethod must be between 1 and 3")
    private Integer payMethod;
}
