package com.common.enums;

public enum NotificationTypeEnum {

    LIKE(1, "点赞了你的笔记"),
    FOLLOW(2, "关注了你"),
    COMMENT(3, "评论了你的笔记"),
    REPLY(4, "回复了你的评论");

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
