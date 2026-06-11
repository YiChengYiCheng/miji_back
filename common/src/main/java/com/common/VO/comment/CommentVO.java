package com.common.VO.comment;

import com.common.VO.note.NoteAuthorVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long noteId;

    private Long parentId;

    private Long rootId;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    private LocalDateTime createTime;

    private NoteAuthorVO author;

    private NoteAuthorVO replyUser;

    private List<CommentVO> replies;
}
