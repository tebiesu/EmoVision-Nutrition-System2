package com.project.healthassistant.common.api;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0, "success"),
    UNAUTHORIZED(1001, "Unauthorized or token invalid"),
    FORBIDDEN(1002, "Forbidden"),
    USERNAME_OR_PASSWORD_ERROR(1003, "Username or password is incorrect"),
    TOKEN_BLACKLISTED(1004, "Token has been revoked"),
    INVALID_PARAM(2001, "Invalid request parameters"),
    MISSING_PARAM(2002, "Missing required parameters"),
    USER_NOT_FOUND(3001, "User not found"),
    USERNAME_EXISTS(3002, "Username already exists"),
    PROFILE_NOT_FOUND(3003, "Health profile not found"),
    RECORD_NOT_FOUND(3004, "Record not found"),
    SYSTEM_ERROR(5000, "Internal server error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
