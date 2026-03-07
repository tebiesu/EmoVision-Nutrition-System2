package com.project.healthassistant.common.api;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0, "success"),
    UNAUTHORIZED(1001, "未登录或令牌无效"),
    FORBIDDEN(1002, "权限不足"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    INVALID_PARAM(2001, "请求参数不合法"),
    MISSING_PARAM(2002, "必填字段缺失"),
    USER_NOT_FOUND(3001, "用户不存在"),
    USERNAME_EXISTS(3002, "用户名已存在"),
    SYSTEM_ERROR(5000, "系统内部异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
