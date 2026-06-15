package com.common.enums;

public enum NotificationTypeEnum {

    LIKE(1, "\u70b9\u8d5e\u4e86\u4f60\u7684\u7b14\u8bb0"),
    FOLLOW(2, "\u5173\u6ce8\u4e86\u4f60"),
    COMMENT(3, "\u8bc4\u8bba\u4e86\u4f60\u7684\u7b14\u8bb0"),
    REPLY(4, "\u56de\u590d\u4e86\u4f60\u7684\u8bc4\u8bba");

    private final Integer type;
    private final String content;

    NotificationTypeEnum(Integer type, String content) {
        this.type = type;
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
