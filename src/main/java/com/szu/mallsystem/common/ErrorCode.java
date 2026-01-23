package com.szu.mallsystem.common;

/**
 * Unified API error codes.
 */
public enum ErrorCode {
    SUCCESS(0, "success"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    CONFLICT(409, "Conflict"),
    VALIDATION_ERROR(422, "Validation failed"),
    SERVER_ERROR(500, "Internal server error"),
    BUSINESS_ERROR(1000, "Business error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
