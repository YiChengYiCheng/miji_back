package com.common.QO.like;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class LikeQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "noteId required")
    private Long noteId;
}
