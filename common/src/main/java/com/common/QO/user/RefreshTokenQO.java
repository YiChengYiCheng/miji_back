package com.common.QO.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class RefreshTokenQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "refreshToken不能为空")
    private String refreshToken;
}

