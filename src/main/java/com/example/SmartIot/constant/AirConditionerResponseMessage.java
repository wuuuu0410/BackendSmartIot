package com.example.SmartIot.constant;

public enum AirConditionerResponseMessage {
    SUCCESS(200, "操作成功"),
    NOT_FOUND(404, "找不到冷氣"),
    INVALID_OPERATION(400, "無效的操作"),
    INTERNAL_ERROR(500, "内部服務器錯誤");

    private final int code;
    private final String message;

    AirConditionerResponseMessage(int code, String message) {
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