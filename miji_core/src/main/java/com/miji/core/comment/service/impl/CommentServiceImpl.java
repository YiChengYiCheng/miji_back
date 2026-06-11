package com.miji.core.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.DO.CommentDO;
import com.common.DO.NoteDO;
import com.common.DO.UserDO;
import com.common.QO.comment.AddCommentQO;
import com.common.QO.comment.CommentListQO;
import com.common.QO.comment.CommentReplyListQO;
import com.common.QO.comment.DeleteCommentQO;
import com.common.VO.comment.CommentVO;
import com.common.VO.note.NoteAuthorVO;
import com.common.enums.CodeEnum;
import com.common.enums.DefaultValue;
import com.common.exception.CustomException;
import com.common.result.Result;
import com.miji.core.comment.mapper.CommentMapper;
import com.miji.core.comment.service.CommentService;
import com.miji.core.note.mapper.NoteMapper;
import com.miji.core.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Long ROOT_COMMENT_ID = 0L;
    private static final int PREVIEW_REPLY_SIZE = 2;

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result add(AddCommentQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        checkActiveNote(qo.getNoteId());

        LocalDateTime now = LocalDateTime.now();
        Long parentId = normalizeParentId(qo.getParentId());
        CommentDO parent = null;
        Long rootId = ROOT_COMMENT_ID;
        Long replyUserId = null;
        if (!ROOT_COMMENT_ID.equals(parentId)) {
            parent = getActiveComment(parentId);
            if (!qo.getNoteId().equals(parent.getNoteId())) {
                throw new CustomException(CodeEnum.COMMON_ERROR.getStatusCode(), "comment note mismatch");
            }
            rootId = ROOT_COMMENT_ID.equals(parent.getRootId()) ? parent.getId() : parent.getRootId();
            replyUserId = parent.getUserId();
        }

        CommentDO commentDO = new CommentDO();
        commentDO.setNoteId(qo.getNoteId());
        commentDO.setUserId(currentUserId);
        commentDO.setParentId(parentId);
        commentDO.setRootId(rootId);
        commentDO.setReplyUserId(replyUserId);
        commentDO.setContent(qo.getContent().trim());
        commentDO.setLikeCount(DefaultValue.NUM_ZERO);
        commentDO.setReplyCount(DefaultValue.NUM_ZERO);
        commentDO.setStatus(DefaultValue.DEFAULT_STATUS);
        commentDO.setCreateTime(now);
        commentDO.setUpdateTime(now);

        int insert = commentMapper.insert(commentDO);
        if (insert <= 0) {
            return Result.fail(CodeEnum.CUSTOM_DATABASE_ERROR_INSERT_FAIL.getStatusCode(), "comment insert fail");
        }

        increaseNoteCommentCount(qo.getNoteId());
        if (parent != null) {
            increaseRootReplyCount(rootId);
        }
        return Result.success(buildCommentVO(commentDO, Collections.emptyMap(), Collections.emptyMap()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delete(DeleteCommentQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        CommentDO comment = getActiveComment(qo.getId());
        if (!currentUserId.equals(comment.getUserId())) {
            throw new CustomException(HttpServletResponse.SC_FORBIDDEN, "no permission");
        }

        int deletedCount;
        LocalDateTime now = LocalDateTime.now();
        if (ROOT_COMMENT_ID.equals(comment.getRootId())) {
            deletedCount = softDeleteRootComment(comment, now);
        } else {
            deletedCount = softDeleteSingleComment(comment, now);
            decreaseRootReplyCount(comment.getRootId());
        }
        decreaseNoteCommentCount(comment.getNoteId(), deletedCount);
        return Result.success(true);
    }

    @Override
    public Result list(CommentListQO qo, Long currentUserId) {
        int page = qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
        int size = qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();

        Page<CommentDO> commentPage = commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<CommentDO>()
                        .eq(CommentDO::getNoteId, qo.getNoteId())
                        .eq(CommentDO::getRootId, ROOT_COMMENT_ID)
                        .eq(CommentDO::getStatus, DefaultValue.DEFAULT_STATUS)
                        .orderByDesc(CommentDO::getCreateTime));
        return Result.success(buildRootCommentPage(commentPage));
    }

    @Override
    public Result replyList(CommentReplyListQO qo, Long currentUserId) {
        int page = qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
        int size = qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();

        CommentDO rootComment = getActiveComment(qo.getRootId());
        if (!ROOT_COMMENT_ID.equals(rootComment.getRootId())) {
            throw new CustomException(CodeEnum.COMMON_ERROR.getStatusCode(), "root comment not found");
        }

        Page<CommentDO> replyPage = commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<CommentDO>()
                        .eq(CommentDO::getRootId, qo.getRootId())
                        .eq(CommentDO::getStatus, DefaultValue.DEFAULT_STATUS)
                        .orderByAsc(CommentDO::getCreateTime));
        return Result.success(buildCommentPage(replyPage));
    }

    private Page<CommentVO> buildRootCommentPage(Page<CommentDO> commentPage) {
        Page<CommentVO> result = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        List<CommentDO> comments = commentPage.getRecords();
        if (comments == null || comments.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        List<Long> rootIds = comments.stream().map(CommentDO::getId).collect(Collectors.toList());
        List<CommentDO> replies = rootIds.stream()
                .flatMap(rootId -> selectPreviewReplies(rootId).stream())
                .collect(Collectors.toList());

        Map<Long, List<CommentDO>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(CommentDO::getRootId));
        UserMaps userMaps = selectUserMaps(comments, replies);

        List<CommentVO> records = comments.stream()
                .map(comment -> {
                    CommentVO commentVO = buildCommentVO(comment, userMaps.userMap, userMaps.replyUserMap);
                    List<CommentDO> previewReplies = replyMap.getOrDefault(comment.getId(), Collections.emptyList());
                    commentVO.setReplies(previewReplies.stream()
                            .map(reply -> buildCommentVO(reply, userMaps.userMap, userMaps.replyUserMap))
                            .collect(Collectors.toList()));
                    return commentVO;
                })
                .collect(Collectors.toList());
        result.setRecords(records);
        return result;
    }

    private Page<CommentVO> buildCommentPage(Page<CommentDO> commentPage) {
        Page<CommentVO> result = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        List<CommentDO> comments = commentPage.getRecords();
        if (comments == null || comments.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        UserMaps userMaps = selectUserMaps(comments, Collections.emptyList());
        result.setRecords(comments.stream()
                .map(comment -> buildCommentVO(comment, userMaps.userMap, userMaps.replyUserMap))
                .collect(Collectors.toList()));
        return result;
    }

    private List<CommentDO> selectPreviewReplies(Long rootId) {
        Page<CommentDO> replyPage = commentMapper.selectPage(new Page<>(DefaultValue.NUM_PAGE, PREVIEW_REPLY_SIZE),
                new LambdaQueryWrapper<CommentDO>()
                        .eq(CommentDO::getRootId, rootId)
                        .eq(CommentDO::getStatus, DefaultValue.DEFAULT_STATUS)
                        .orderByAsc(CommentDO::getCreateTime));
        return replyPage.getRecords();
    }

    private UserMaps selectUserMaps(List<CommentDO> comments, List<CommentDO> replies) {
        List<CommentDO> allComments = new ArrayList<>();
        allComments.addAll(comments);
        allComments.addAll(replies);

        Set<Long> userIds = allComments.stream()
                .map(CommentDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserDO> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity(), (first, second) -> first));

        Set<Long> replyUserIds = allComments.stream()
                .map(CommentDO::getReplyUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserDO> replyUserMap = replyUserIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(replyUserIds).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity(), (first, second) -> first));
        return new UserMaps(userMap, replyUserMap);
    }

    private CommentVO buildCommentVO(CommentDO commentDO, Map<Long, UserDO> userMap, Map<Long, UserDO> replyUserMap) {
        CommentVO commentVO = new CommentVO();
        commentVO.setId(commentDO.getId());
        commentVO.setNoteId(commentDO.getNoteId());
        commentVO.setParentId(commentDO.getParentId());
        commentVO.setRootId(commentDO.getRootId());
        commentVO.setContent(commentDO.getContent());
        commentVO.setLikeCount(commentDO.getLikeCount());
        commentVO.setReplyCount(commentDO.getReplyCount());
        commentVO.setCreateTime(commentDO.getCreateTime());

        UserDO author = userMap.get(commentDO.getUserId());
        if (author == null && commentDO.getUserId() != null) {
            author = userMapper.selectById(commentDO.getUserId());
        }
        commentVO.setAuthor(buildNoteAuthorVO(author));

        UserDO replyUser = replyUserMap.get(commentDO.getReplyUserId());
        if (replyUser == null && commentDO.getReplyUserId() != null) {
            replyUser = userMapper.selectById(commentDO.getReplyUserId());
        }
        commentVO.setReplyUser(buildNoteAuthorVO(replyUser));
        return commentVO;
    }

    private NoteAuthorVO buildNoteAuthorVO(UserDO userDO) {
        if (userDO == null) {
            return null;
        }
        NoteAuthorVO authorVO = new NoteAuthorVO();
        authorVO.setId(userDO.getId());
        authorVO.setNickname(userDO.getNickname());
        authorVO.setAvatar(userDO.getAvatar());
        return authorVO;
    }

    private int softDeleteRootComment(CommentDO comment, LocalDateTime now) {
        int deleted = commentMapper.update(null, new LambdaUpdateWrapper<CommentDO>()
                .eq(CommentDO::getId, comment.getId())
                .eq(CommentDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .set(CommentDO::getStatus, DefaultValue.NUM_ZERO)
                .set(CommentDO::getUpdateTime, now));
        Long replyCount = commentMapper.selectCount(new LambdaQueryWrapper<CommentDO>()
                .eq(CommentDO::getRootId, comment.getId())
                .eq(CommentDO::getStatus, DefaultValue.DEFAULT_STATUS));
        if (replyCount != null && replyCount > 0) {
            commentMapper.update(null, new LambdaUpdateWrapper<CommentDO>()
                    .eq(CommentDO::getRootId, comment.getId())
                    .eq(CommentDO::getStatus, DefaultValue.DEFAULT_STATUS)
                    .set(CommentDO::getStatus, DefaultValue.NUM_ZERO)
                    .set(CommentDO::getUpdateTime, now));
            deleted += replyCount.intValue();
        }
        return deleted;
    }

    private int softDeleteSingleComment(CommentDO comment, LocalDateTime now) {
        return commentMapper.update(null, new LambdaUpdateWrapper<CommentDO>()
                .eq(CommentDO::getId, comment.getId())
                .eq(CommentDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .set(CommentDO::getStatus, DefaultValue.NUM_ZERO)
                .set(CommentDO::getUpdateTime, now));
    }

    private void increaseNoteCommentCount(Long noteId) {
        noteMapper.update(null, new LambdaUpdateWrapper<NoteDO>()
                .eq(NoteDO::getId, noteId)
                .setSql("comment_count = IFNULL(comment_count, 0) + 1")
                .setSql("score = IFNULL(score, 0) + " + DefaultValue.COMMENT_SCORE_WEIGHT.toPlainString()));
    }

    private void decreaseNoteCommentCount(Long noteId, int count) {
        if (count <= 0) {
            return;
        }
        String scoreDecrease = DefaultValue.COMMENT_SCORE_WEIGHT
                .multiply(BigDecimal.valueOf(count))
                .toPlainString();
        noteMapper.update(null, new LambdaUpdateWrapper<NoteDO>()
                .eq(NoteDO::getId, noteId)
                .setSql("comment_count = GREATEST(IFNULL(comment_count, 0) - " + count + ", 0)")
                .setSql("score = GREATEST(IFNULL(score, 0) - " + scoreDecrease + ", 0)"));
    }

    private void increaseRootReplyCount(Long rootId) {
        commentMapper.update(null, new LambdaUpdateWrapper<CommentDO>()
                .eq(CommentDO::getId, rootId)
                .setSql("reply_count = IFNULL(reply_count, 0) + 1"));
    }

    private void decreaseRootReplyCount(Long rootId) {
        commentMapper.update(null, new LambdaUpdateWrapper<CommentDO>()
                .eq(CommentDO::getId, rootId)
                .setSql("reply_count = GREATEST(IFNULL(reply_count, 0) - 1, 0)"));
    }

    private CommentDO getActiveComment(Long commentId) {
        CommentDO comment = commentMapper.selectById(commentId);
        if (comment == null || DefaultValue.NUM_ZERO.equals(comment.getStatus())) {
            throw new CustomException(CodeEnum.COMMON_ERROR.getStatusCode(), "comment not found");
        }
        return comment;
    }

    private void checkActiveNote(Long noteId) {
        NoteDO noteDO = noteMapper.selectById(noteId);
        if (noteDO == null || DefaultValue.NUM_ZERO.equals(noteDO.getStatus())) {
            throw new CustomException(CodeEnum.COMMON_ERROR.getStatusCode(), "note not found");
        }
    }

    private void checkLoginUser(Long currentUserId) {
        if (currentUserId == null) {
            throw new CustomException(HttpServletResponse.SC_UNAUTHORIZED, "please login first");
        }
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_COMMENT_ID : parentId;
    }

    private static class UserMaps {
        private final Map<Long, UserDO> userMap;
        private final Map<Long, UserDO> replyUserMap;

        private UserMaps(Map<Long, UserDO> userMap, Map<Long, UserDO> replyUserMap) {
            this.userMap = userMap;
            this.replyUserMap = replyUserMap;
        }
    }
}
