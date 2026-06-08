package com.common.VO.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String nickname;

    private String avatar;

    private String bio;

    private Integer fansCount;

    private Integer followCount;

    private Boolean followed;
}
