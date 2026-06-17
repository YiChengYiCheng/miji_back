package com.common.QO.message;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DeleteMessageQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "messageId can not be null")
    private Long messageId;
}
