package com.szu.mallsystem.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentCallbackRequest {
    @NotBlank(message = "支付单号不能为空")
    private String payNo;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String transactionId;
}
