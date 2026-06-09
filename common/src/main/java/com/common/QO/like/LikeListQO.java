package com.common.QO.like;

import lombok.Data;

import java.io.Serializable;

@Data
public class LikeListQO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page;

    private Integer size;
}
