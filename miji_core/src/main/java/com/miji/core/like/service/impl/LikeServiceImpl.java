package com.miji.core.like.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.DO.LikeRecordDO;
import com.common.DO.NoteDO;
import com.common.DO.UserDO;
import com.common.QO.like.LikeListQO;
import com.common.QO.like.LikeQO;
import com.common.VO.note.NoteAuthorVO;
import com.common.VO.note.NoteListVO;
import com.common.enums.CodeEnum;
import com.common.enums.DefaultValue;
import com.common.exception.CustomException;
import com.common.result.Result;
import com.miji.core.like.mapper.LikeRecordMapper;
import com.miji.core.like.service.LikeService;
import com.miji.core.note.mapper.NoteMapper;
import com.miji.core.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result add(LikeQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        checkActiveNote(qo.getNoteId());

        if (exists(currentUserId, qo.getNoteId())) {
            return Result.success(true);
        }

        LikeRecordDO likeRecordDO = new LikeRecordDO();
        likeRecordDO.setUserId(currentUserId);
        likeRecordDO.setNoteId(qo.getNoteId());
        likeRecordDO.setCreateTime(LocalDateTime.now());

        try {
            int insert = likeRecordMapper.insert(likeRecordDO);
            if (insert <= 0) {
                return Result.fail(CodeEnum.CUSTOM_DATABASE_ERROR_INSERT_FAIL.getStatusCode(), "like insert fail");
            }
        } catch (DuplicateKeyException e) {
            return Result.success(true);
        }

        increaseLikeCount(qo.getNoteId());
        return Result.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result cancel(LikeQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);

        int delete = likeRecordMapper.delete(new LambdaQueryWrapper<LikeRecordDO>()
                .eq(LikeRecordDO::getUserId, currentUserId)
                .eq(LikeRecordDO::getNoteId, qo.getNoteId()));
        if (delete <= 0) {
            return Result.success(true);
        }

        decreaseLikeCount(qo.getNoteId());
        return Result.success(true);
    }

    @Override
    public Result status(LikeQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        return Result.success(exists(currentUserId, qo.getNoteId()));
    }

    @Override
    public Result list(LikeListQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        int page = qo == null || qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
        int size = qo == null || qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();

        Page<LikeRecordDO> likePage = likeRecordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<LikeRecordDO>()
                        .eq(LikeRecordDO::getUserId, currentUserId)
                        .orderByDesc(LikeRecordDO::getCreateTime));
        return Result.success(buildLikedNotePage(likePage));
    }

    private boolean exists(Long userId, Long noteId) {
        Long count = likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecordDO>()
                .eq(LikeRecordDO::getUserId, userId)
                .eq(LikeRecordDO::getNoteId, noteId));
        return count != null && count > 0;
    }

    private void checkLoginUser(Long currentUserId) {
        if (currentUserId == null) {
            throw new CustomException(HttpServletResponse.SC_UNAUTHORIZED, "please login first");
        }
    }

    private void checkActiveNote(Long noteId) {
        NoteDO noteDO = noteMapper.selectById(noteId);
        if (noteDO == null || DefaultValue.NUM_ZERO.equals(noteDO.getStatus())) {
            throw new CustomException(CodeEnum.COMMON_ERROR.getStatusCode(), "note not found");
        }
    }

    private void increaseLikeCount(Long noteId) {
        noteMapper.update(null, new LambdaUpdateWrapper<NoteDO>()
                .eq(NoteDO::getId, noteId)
                .setSql("like_count = IFNULL(like_count, 0) + 1")
                .setSql("score = IFNULL(score, 0) + " + DefaultValue.LIKE_SCORE_WEIGHT.toPlainString()));
    }

    private void decreaseLikeCount(Long noteId) {
        noteMapper.update(null, new LambdaUpdateWrapper<NoteDO>()
                .eq(NoteDO::getId, noteId)
                .setSql("like_count = GREATEST(IFNULL(like_count, 0) - 1, 0)")
                .setSql("score = GREATEST(IFNULL(score, 0) - " + DefaultValue.LIKE_SCORE_WEIGHT.toPlainString() + ", 0)"));
    }

    private Page<NoteListVO> buildLikedNotePage(Page<LikeRecordDO> likePage) {
        Page<NoteListVO> result = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());
        List<LikeRecordDO> records = likePage.getRecords();
        if (records == null || records.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        List<Long> noteIds = records.stream()
                .map(LikeRecordDO::getNoteId)
                .collect(Collectors.toList());
        Map<Long, NoteDO> noteMap = noteMapper.selectBatchIds(noteIds).stream()
                .filter(note -> DefaultValue.DEFAULT_STATUS.equals(note.getStatus()))
                .collect(Collectors.toMap(NoteDO::getId, Function.identity(), (first, second) -> first));

        List<Long> userIds = noteMap.values().stream()
                .map(NoteDO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserDO> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity(), (first, second) -> first));

        List<NoteListVO> noteRecords = noteIds.stream()
                .map(noteMap::get)
                .filter(note -> note != null)
                .map(note -> buildNoteListVO(note, userMap.get(note.getUserId())))
                .collect(Collectors.toList());
        result.setRecords(noteRecords);
        return result;
    }

    private NoteListVO buildNoteListVO(NoteDO noteDO, UserDO userDO) {
        NoteListVO noteListVO = new NoteListVO();
        noteListVO.setNoteInfo(noteDO);
        noteListVO.setAuthor(buildNoteAuthorVO(userDO));
        noteListVO.setLiked(true);
        return noteListVO;
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
}
