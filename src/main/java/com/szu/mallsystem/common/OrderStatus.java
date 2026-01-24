package com.szu.mallsystem.common;

public enum OrderStatus {
    PENDING(1, "Pending payment"),
    PAID(2, "Paid"),
    SHIPPED(3, "Shipped"),
    COMPLETED(4, "Completed"),
    CANCELED(5, "Canceled");

    private final int code;
    private final String label;

    OrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
