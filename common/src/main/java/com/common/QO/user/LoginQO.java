package com.common.QO.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class LoginQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户账号不能为空")
    private String account;

    //密码(加密存储)
    @NotNull(message = "密码不能为空")
    private String password;

}
