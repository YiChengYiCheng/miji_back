package com.miji.core.like.controller;

import com.common.QO.like.LikeListQO;
import com.common.QO.like.LikeQO;
import com.common.result.Result;
import com.miji.core.like.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/add")
    public Result add(@RequestBody @Valid LikeQO qo, HttpServletRequest request) {
        return likeService.add(qo, getUserId(request));
    }

    @PostMapping("/cancel")
    public Result cancel(@RequestBody @Valid LikeQO qo, HttpServletRequest request) {
        return likeService.cancel(qo, getUserId(request));
    }

    @PostMapping("/status")
    public Result status(@RequestBody @Valid LikeQO qo, HttpServletRequest request) {
        return likeService.status(qo, getUserId(request));
    }

    @PostMapping("/list")
    public Result list(@RequestBody(required = false) LikeListQO qo, HttpServletRequest request) {
        return likeService.list(qo, getUserId(request));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
