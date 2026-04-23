package com.mall.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "success"),
    FAILED(500, "failed"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    VALIDATE_FAILED(400, "参数错误"),
    BUSINESS(1000, "业务错误"),
    CAPTCHA_ERROR(1001, "图形验证码错误"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PHONE_REGISTERED(1003, "手机号已注册"),
    LOGIN_ERROR(1004, "登录失败"),
    TOKEN_INVALID(1005, "Token无效或过期"),
    STOCK_NOT_ENOUGH(2001, "库存不足"),
    ORDER_NOT_FOUND(2002, "订单不存在"),
    ORDER_STATUS_ERROR(2003, "订单状态不允许该操作");

    private final int code;
    private final String message;
}
