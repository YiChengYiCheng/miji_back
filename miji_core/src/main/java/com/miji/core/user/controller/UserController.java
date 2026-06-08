package com.miji.core.user.controller;

import com.common.QO.user.LoginQO;
import com.common.QO.user.RegisterQO;
import com.common.QO.user.RefreshTokenQO;
import com.common.result.Result;
import com.miji.core.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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

    @PostMapping("/register")
    public Result register(@RequestBody @Valid RegisterQO qo) {
        return userService.register(qo);
    }

    @PostMapping("/refresh")
    public Result refreshToken(@RequestBody @Valid RefreshTokenQO qo) {
        return userService.refreshToken(qo);
    }

    @GetMapping("/me")
    public Result me(HttpServletRequest request) {
        return userService.me(getUserId(request));
    }

    @GetMapping("/detail")
    public Result detail(@RequestParam Long userId, HttpServletRequest request) {
        return userService.detail(userId, getUserId(request));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
