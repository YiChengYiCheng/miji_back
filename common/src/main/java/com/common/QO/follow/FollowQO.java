package com.common.QO.follow;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FollowQO {

    @NotNull(message = "followUserId required")
    private Long followUserId;
}
