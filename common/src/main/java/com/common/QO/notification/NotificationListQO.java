package com.common.QO.notification;

import lombok.Data;

import java.io.Serializable;

@Data
public class NotificationListQO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page;

    private Integer size;

    private Integer type;

    private Integer isRead;
}
