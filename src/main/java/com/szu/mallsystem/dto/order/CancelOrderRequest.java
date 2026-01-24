package com.szu.mallsystem.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelOrderRequest {
    @Size(max = 255, message = "reason length must be <= 255")
    private String reason;
}
