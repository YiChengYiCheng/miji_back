package com.common.QO.comment;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CommentListQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "noteId required")
    private Long noteId;

    private Integer page;

    private Integer size;
}
