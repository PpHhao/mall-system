package com.szu.mallsystem.dto.payment;

import lombok.Data;

@Data
public class ProcessRefundRequest {
    private Boolean approved;

    private String rejectReason;
}
