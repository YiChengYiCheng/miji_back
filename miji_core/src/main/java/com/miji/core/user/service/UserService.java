package com.miji.core.user.service;

import com.common.QO.user.LoginQO;
import com.common.QO.user.RegisterQO;
import com.common.QO.user.RefreshTokenQO;
import com.common.result.Result;

import javax.validation.Valid;

public interface UserService {
    Result login(@Valid LoginQO qo);

    Result register(@Valid RegisterQO qo);

    Result refreshToken(@Valid RefreshTokenQO qo);

    Result me(Long currentUserId);

    Result detail(Long userId, Long currentUserId);
}
