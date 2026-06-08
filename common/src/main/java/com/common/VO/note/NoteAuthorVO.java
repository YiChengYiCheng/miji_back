package com.common.VO.note;

import lombok.Data;

import java.io.Serializable;

@Data
public class NoteAuthorVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String nickname;

    private String avatar;
}
