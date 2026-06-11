package com.common.QO.comment;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CommentReplyListQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "rootId required")
    private Long rootId;

    private Integer page;

    private Integer size;
}
