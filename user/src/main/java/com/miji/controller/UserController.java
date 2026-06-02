package com.miji.controller;

import com.common.QO.user.LoginQO;
import com.common.QO.user.RefreshTokenQO;
import com.common.result.Result;
import com.miji.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody @Valid LoginQO qo){
        return userService.login(qo);
    }

    @PostMapping("/refresh")
    public Result refreshToken(@RequestBody @Valid RefreshTokenQO qo) {
        return userService.refreshToken(qo);
    }
}
