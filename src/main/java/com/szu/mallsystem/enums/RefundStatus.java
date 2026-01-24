package com.szu.mallsystem.enums;

import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
public enum RefundStatus {
    PENDING(0, "申请中"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    COMPLETED(3, "已完成");

    private final Integer code;
    private final String name;

    RefundStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RefundStatus fromCode(Integer code) {
        for (RefundStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
