package com.szu.mallsystem.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
public enum PaymentStatus {
    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    REFUNDING(2, "退款中"),
    REFUNDED(3, "已退款"),
    FAILED(4, "支付失败");

    private final Integer code;
    private final String name;

    PaymentStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PaymentStatus fromCode(Integer code) {
        for (PaymentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return UNPAID;
    }
}
