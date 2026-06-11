package com.common.QO.comment;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DeleteCommentQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "id required")
    private Long id;
}
