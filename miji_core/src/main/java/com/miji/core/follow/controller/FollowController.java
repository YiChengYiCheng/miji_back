package com.miji.core.follow.controller;

import com.common.QO.follow.FollowListQO;
import com.common.QO.follow.FollowQO;
import com.common.result.Result;
import com.miji.core.follow.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/add")
    public Result add(@RequestBody @Valid FollowQO qo, HttpServletRequest request) {
        return followService.add(qo, getUserId(request));
    }

    @PostMapping("/cancel")
    public Result cancel(@RequestBody @Valid FollowQO qo, HttpServletRequest request) {
        return followService.cancel(qo, getUserId(request));
    }

    @PostMapping("/status")
    public Result status(@RequestBody @Valid FollowQO qo, HttpServletRequest request) {
        return followService.status(qo, getUserId(request));
    }

    @PostMapping("/following/list")
    public Result followingList(@RequestBody(required = false) FollowListQO qo, HttpServletRequest request) {
        return followService.followingList(qo, getUserId(request));
    }

    @PostMapping("/fans/list")
    public Result fansList(@RequestBody(required = false) FollowListQO qo, HttpServletRequest request) {
        return followService.fansList(qo, getUserId(request));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
