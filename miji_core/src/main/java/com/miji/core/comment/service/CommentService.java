package com.miji.core.comment.service;

import com.common.QO.comment.AddCommentQO;
import com.common.QO.comment.CommentListQO;
import com.common.QO.comment.CommentReplyListQO;
import com.common.QO.comment.DeleteCommentQO;
import com.common.result.Result;

import javax.validation.Valid;

public interface CommentService {

    Result add(@Valid AddCommentQO qo, Long currentUserId);

    Result delete(@Valid DeleteCommentQO qo, Long currentUserId);

    Result list(@Valid CommentListQO qo, Long currentUserId);

    Result replyList(@Valid CommentReplyListQO qo, Long currentUserId);
}
