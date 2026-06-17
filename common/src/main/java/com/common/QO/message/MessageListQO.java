package com.common.QO.message;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class MessageListQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "conversationId can not be null")
    private Long conversationId;

    private Integer page;

    private Integer size;
}
