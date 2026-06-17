package com.common.QO.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConversationListQO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page;

    private Integer size;
}
