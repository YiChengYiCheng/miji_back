package com.common.VO.note;

import com.common.DO.NoteDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class NoteVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private NoteDO noteInfo;

    private NoteAuthorVO author;

    private List<String> images;

    private Boolean liked;
}
