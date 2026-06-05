package com.common.QO.note;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DeleteNoteQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "id cannot be null")
    private Long id;
}
