package com.common.QO.comment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class AddCommentQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "noteId required")
    private Long noteId;

    private Long parentId;

    @NotBlank(message = "content required")
    @Size(max = 500, message = "content max length is 500")
    private String content;
}
