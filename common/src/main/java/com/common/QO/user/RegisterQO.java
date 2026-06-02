package com.common.QO.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class RegisterQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户账号不能为空")
    private String account;

    @NotNull(message = "密码不能为空")
    private String password;

    @NotNull(message = "验证码不能为空")
    private String code;
}
