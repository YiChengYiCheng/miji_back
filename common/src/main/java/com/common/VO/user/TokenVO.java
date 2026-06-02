package com.common.VO.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}

