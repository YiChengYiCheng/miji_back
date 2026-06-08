package com.miji.core.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.DO.NoteImageDO;
import com.common.DO.NoteDO;
import com.common.DO.UserDO;
import com.common.QO.note.AddNoteQO;
import com.common.QO.note.DeleteNoteQO;
import com.common.QO.note.NoteDetailQO;
import com.common.QO.note.NoteListQO;
import com.common.QO.note.UpdateNoteQO;
import com.common.VO.note.NoteAuthorVO;
import com.common.VO.note.NoteListVO;
import com.common.VO.note.NoteVO;
import com.common.enums.CodeEnum;
import com.common.enums.DefaultValue;
import com.common.exception.CustomException;
import com.common.result.Result;
import com.miji.core.note.mapper.NoteImageMapper;
import com.miji.core.note.mapper.NoteMapper;
import com.miji.core.note.service.NoteService;
import com.miji.core.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private NoteImageMapper noteImageMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result add(AddNoteQO qo, Long currentUserId) {
        LocalDateTime now = LocalDateTime.now();
        NoteDO noteDO = new NoteDO();
        noteDO.setUserId(currentUserId);
        noteDO.setTitle(qo.getTitle());
        noteDO.setContent(qo.getContent());
        noteDO.setCover(qo.getCover());
        noteDO.setViewCount(defaultCount(qo.getViewCount()));
        noteDO.setLikeCount(defaultCount(qo.getLikeCount()));
        noteDO.setCollectCount(defaultCount(qo.getCollectCount()));
        noteDO.setCommentCount(defaultCount(qo.getCommentCount()));
        noteDO.setScore(calculateScore(noteDO));
        noteDO.setStatus(DefaultValue.DEFAULT_STATUS);
        noteDO.setCreateTime(now);
        noteDO.setUpdateTime(now);

        int insert = noteMapper.insert(noteDO);
        if (insert <= 0) {
            return Result.fail(CodeEnum.CUSTOM_DATABASE_ERROR_INSERT_FAIL.getStatusCode(), "note insert fail");
        }
        saveNoteImages(noteDO.getId(), qo.getImages(), now);
        return Result.success(noteDO);
    }

    @Override
    public Result delete(DeleteNoteQO qo, Long currentUserId) {
        NoteDO noteDO = getOwnedNote(qo.getId(), currentUserId);
        noteDO.setStatus(DefaultValue.NUM_ZERO);
        noteDO.setUpdateTime(LocalDateTime.now());
        int update = noteMapper.updateById(noteDO);
        if (update <= 0) {
            return Result.fail(CodeEnum.CUSTOM_DATABASE_ERROR_UPDATE_FAIL.getStatusCode(), "note delete fail");
        }
        return Result.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result update(UpdateNoteQO qo, Long currentUserId) {
        NoteDO noteDO = getOwnedNote(qo.getId(), currentUserId);
        if (qo.getTitle() != null) {
            noteDO.setTitle(qo.getTitle());
        }
        if (qo.getContent() != null) {
            noteDO.setContent(qo.getContent());
        }
        if (qo.getCover() != null) {
            noteDO.setCover(qo.getCover());
        }
        if (qo.getViewCount() != null) {
            noteDO.setViewCount(qo.getViewCount());
        }
        if (qo.getLikeCount() != null) {
            noteDO.setLikeCount(qo.getLikeCount());
        }
        if (qo.getCollectCount() != null) {
            noteDO.setCollectCount(qo.getCollectCount());
        }
        if (qo.getCommentCount() != null) {
            noteDO.setCommentCount(qo.getCommentCount());
        }
        noteDO.setScore(calculateScore(noteDO));
        noteDO.setUpdateTime(LocalDateTime.now());

        int update = noteMapper.updateById(noteDO);
        if (update <= 0) {
            return Result.fail(CodeEnum.CUSTOM_DATABASE_ERROR_UPDATE_FAIL.getStatusCode(), "note update fail");
        }
        if (qo.getImages() != null) {
            noteImageMapper.delete(new LambdaQueryWrapper<NoteImageDO>()
                    .eq(NoteImageDO::getNoteId, noteDO.getId()));
            saveNoteImages(noteDO.getId(), qo.getImages(), noteDO.getUpdateTime());
        }
        return Result.success(buildNoteVO(noteDO));
    }

    @Override
    public Result detail(NoteDetailQO qo) {
        NoteDO noteDO = noteMapper.selectById(qo.getId());
        if (noteDO == null || DefaultValue.NUM_ZERO.equals(noteDO.getStatus())) {
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "note not found");
        }
        return Result.success(buildNoteVO(noteDO));
    }

    @Override
    public Result list(NoteListQO qo) {
        int page = qo == null || qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
        int size = qo == null || qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();

        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<NoteDO>()
                .eq(NoteDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .orderByDesc(NoteDO::getScore)
                .orderByDesc(NoteDO::getCreateTime);
        if (qo != null && qo.getUserId() != null) {
            wrapper.eq(NoteDO::getUserId, qo.getUserId());
        }

        Page<NoteDO> notePage = noteMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(buildNoteListPage(notePage));
    }

    private BigDecimal calculateScore(NoteDO noteDO) {
        return BigDecimal.valueOf(defaultCount(noteDO.getViewCount())).multiply(DefaultValue.VIEW_SCORE_WEIGHT)
                .add(BigDecimal.valueOf(defaultCount(noteDO.getLikeCount())).multiply(DefaultValue.LIKE_SCORE_WEIGHT))
                .add(BigDecimal.valueOf(defaultCount(noteDO.getCollectCount())).multiply(DefaultValue.COLLECT_SCORE_WEIGHT))
                .add(BigDecimal.valueOf(defaultCount(noteDO.getCommentCount())).multiply(DefaultValue.COMMENT_SCORE_WEIGHT));
    }

    private Integer defaultCount(Integer count) {
        return count == null ? DefaultValue.NUM_ZERO : count;
    }

    private void saveNoteImages(Long noteId, List<String> images, LocalDateTime createTime) {
        if (noteId == null || images == null || images.isEmpty()) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            String imageUrl = images.get(i);
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                continue;
            }
            NoteImageDO noteImageDO = new NoteImageDO();
            noteImageDO.setNoteId(noteId);
            noteImageDO.setImageUrl(imageUrl.trim());
            noteImageDO.setSortNum(i);
            noteImageDO.setCreateTime(createTime);
            noteImageMapper.insert(noteImageDO);
        }
    }

    private NoteDO getOwnedNote(Long noteId, Long currentUserId) {
        NoteDO noteDO = noteMapper.selectById(noteId);
        if (noteDO == null || DefaultValue.NUM_ZERO.equals(noteDO.getStatus())) {
            throw new CustomException(CodeEnum.COMMON_ERROR.getStatusCode(), "note not found");
        }
        if (currentUserId == null || !currentUserId.equals(noteDO.getUserId())) {
            throw new CustomException(HttpServletResponse.SC_FORBIDDEN, "no permission");
        }
        return noteDO;
    }

    private NoteVO buildNoteVO(NoteDO noteDO) {
        NoteVO noteVO = new NoteVO();
        noteVO.setNoteInfo(noteDO);
        List<String> images = noteImageMapper.selectList(new LambdaQueryWrapper<NoteImageDO>()
                        .eq(NoteImageDO::getNoteId, noteDO.getId())
                        .orderByAsc(NoteImageDO::getSortNum))
                .stream()
                .map(NoteImageDO::getImageUrl)
                .collect(Collectors.toList());
        noteVO.setImages(images);
        return noteVO;
    }

    private Page<NoteListVO> buildNoteListPage(Page<NoteDO> notePage) {
        Page<NoteListVO> result = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        List<NoteDO> notes = notePage.getRecords();
        if (notes == null || notes.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        List<Long> userIds = notes.stream()
                .map(NoteDO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserDO> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity(), (first, second) -> first));

        List<NoteListVO> records = notes.stream()
                .map(note -> buildNoteListVO(note, userMap.get(note.getUserId())))
                .collect(Collectors.toList());
        result.setRecords(records);
        return result;
    }

    private NoteListVO buildNoteListVO(NoteDO noteDO, UserDO userDO) {
        NoteListVO noteListVO = new NoteListVO();
        noteListVO.setNoteInfo(noteDO);
        noteListVO.setAuthor(buildNoteAuthorVO(userDO));
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
