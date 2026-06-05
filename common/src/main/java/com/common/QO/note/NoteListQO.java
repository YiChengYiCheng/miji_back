package com.common.QO.note;

import lombok.Data;

import java.io.Serializable;

@Data
public class NoteListQO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Integer page;

    private Integer size;
}
