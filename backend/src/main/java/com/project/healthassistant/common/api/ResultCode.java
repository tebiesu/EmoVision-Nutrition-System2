package com.project.healthassistant.common.api;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0, "成功"),
    UNAUTHORIZED(1001, "未授权或登录已失效"),
    FORBIDDEN(1002, "无权限访问"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    TOKEN_BLACKLISTED(1004, "登录凭证已失效"),
    INVALID_PARAM(2001, "请求参数不合法"),
    MISSING_PARAM(2002, "缺少必要参数"),
    USER_NOT_FOUND(3001, "用户不存在"),
    USERNAME_EXISTS(3002, "用户名已存在"),
    PROFILE_NOT_FOUND(3003, "健康档案不存在"),
    RECORD_NOT_FOUND(3004, "记录不存在"),
    SYSTEM_ERROR(5000, "系统内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
