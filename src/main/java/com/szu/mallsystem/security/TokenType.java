package com.szu.mallsystem.security;

public enum TokenType {
    ACCESS(1),
    REFRESH(2);

    private final int code;

    TokenType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TokenType fromCode(int code) {
        for (TokenType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
