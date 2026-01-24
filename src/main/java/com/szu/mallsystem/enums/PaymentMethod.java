package com.szu.mallsystem.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
public enum PaymentMethod {
    MOCK(1, "模拟支付"),
    ALIPAY(2, "支付宝(模拟)"),
    WECHAT(3, "微信支付(模拟)");

    private final Integer code;
    private final String name;

    PaymentMethod(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PaymentMethod fromCode(Integer code) {
        for (PaymentMethod method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return MOCK;
    }
}
