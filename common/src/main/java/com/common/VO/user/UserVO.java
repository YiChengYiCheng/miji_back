package com.common.VO.user;

import com.common.DO.UserDO;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    //用户基本信息
    private UserDO userInfo;
    //access token
    private String accessToken;
    //refresh token
    private String refreshToken;
    //access token过期时间，单位秒
    private Long expiresIn;
}
