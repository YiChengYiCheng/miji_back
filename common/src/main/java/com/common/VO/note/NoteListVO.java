package com.common.VO.note;

import com.common.DO.NoteDO;
import lombok.Data;

import java.io.Serializable;

@Data
public class NoteListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private NoteDO noteInfo;

    private NoteAuthorVO author;

    private Boolean liked;
}
