package com.common.VO.message;

import com.common.VO.note.NoteAuthorVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ConversationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private NoteAuthorVO peerUser;

    private Long lastMessageId;

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
