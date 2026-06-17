package com.common.VO.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MessageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long conversationId;

    private Long senderUserId;

    private Long receiverUserId;

    private String content;

    private Integer isRead;

    private LocalDateTime createTime;
}
