package com.nova.enumeration;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    PENDING_PAYMENT(1, "待付款"),
    TO_BE_CONFIRMED(2, "待接单"),
    CONFIRMED(3, "已接单"),
    DELIVERY_IN_PROGRESS(4, "配送中"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }

    public boolean is(Integer statusCode) {
        return statusCode != null && this.code == statusCode;
    }

    public boolean not(Integer statusCode) {
        return !is(statusCode);
    }
}
