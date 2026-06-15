package com.common.VO.notification;

import com.common.VO.note.NoteAuthorVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotificationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer type;

    private Long noteId;

    private Long commentId;

    private String content;

    private Integer isRead;

    private LocalDateTime createTime;

    private NoteAuthorVO sender;
}
