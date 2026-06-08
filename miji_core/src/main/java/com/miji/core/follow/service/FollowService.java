package com.miji.core.follow.service;

import com.common.QO.follow.FollowListQO;
import com.common.QO.follow.FollowQO;
import com.common.result.Result;

import javax.validation.Valid;

public interface FollowService {

    Result add(@Valid FollowQO qo, Long currentUserId);

    Result cancel(@Valid FollowQO qo, Long currentUserId);

    Result status(@Valid FollowQO qo, Long currentUserId);

    Result followingList(FollowListQO qo, Long currentUserId);

    Result fansList(FollowListQO qo, Long currentUserId);
}
