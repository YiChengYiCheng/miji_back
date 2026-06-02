package com.miji.service;

import com.common.QO.user.LoginQO;
import com.common.QO.user.RefreshTokenQO;
import com.common.result.Result;

import javax.validation.Valid;

public interface UserService {
    Result login(@Valid LoginQO qo);

    Result refreshToken(@Valid RefreshTokenQO qo);
}
