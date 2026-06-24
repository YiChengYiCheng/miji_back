package com.common.QO.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateUserQO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nickname;

    private String avatar;

    private String bio;
}
