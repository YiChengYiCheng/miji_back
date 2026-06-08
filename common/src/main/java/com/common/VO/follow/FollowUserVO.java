package com.common.VO.follow;

import lombok.Data;

@Data
public class FollowUserVO {

    private Long id;

    private String nickname;

    private String avatar;

    private String bio;

    private Integer fansCount;

    private Integer followCount;

    private Boolean followed;
}
