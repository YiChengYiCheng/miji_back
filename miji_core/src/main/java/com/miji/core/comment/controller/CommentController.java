package com.miji.core.comment.controller;

import com.common.QO.comment.AddCommentQO;
import com.common.QO.comment.CommentListQO;
import com.common.QO.comment.CommentReplyListQO;
import com.common.QO.comment.DeleteCommentQO;
import com.common.result.Result;
import com.miji.annotation.OptionalLogin;
import com.miji.core.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public Result add(@RequestBody @Valid AddCommentQO qo, HttpServletRequest request) {
        return commentService.add(qo, getUserId(request));
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody @Valid DeleteCommentQO qo, HttpServletRequest request) {
        return commentService.delete(qo, getUserId(request));
    }

    @PostMapping("/list")
    @OptionalLogin
    public Result list(@RequestBody @Valid CommentListQO qo, HttpServletRequest request) {
        return commentService.list(qo, getUserId(request));
    }

    @PostMapping("/reply/list")
    public Result replyList(@RequestBody @Valid CommentReplyListQO qo, HttpServletRequest request) {
        return commentService.replyList(qo, getUserId(request));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
