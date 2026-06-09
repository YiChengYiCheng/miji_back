package com.miji.core.like.service;

import com.common.QO.like.LikeListQO;
import com.common.QO.like.LikeQO;
import com.common.result.Result;

import javax.validation.Valid;

public interface LikeService {

    Result add(@Valid LikeQO qo, Long currentUserId);

    Result cancel(@Valid LikeQO qo, Long currentUserId);

    Result status(@Valid LikeQO qo, Long currentUserId);

    Result list(LikeListQO qo, Long currentUserId);
}
