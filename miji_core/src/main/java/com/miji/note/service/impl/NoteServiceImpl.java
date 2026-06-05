package com.miji.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.DO.NoteImageDO;
import com.common.DO.NoteDO;
import com.common.QO.note.AddNoteQO;
import com.common.QO.note.DeleteNoteQO;
import com.common.QO.note.NoteDetailQO;
import com.common.QO.note.NoteListQO;
import com.common.QO.note.UpdateNoteQO;
import com.common.enums.CodeEnum;
import com.common.enums.DefaultValue;
import com.common.result.Result;
import com.miji.note.mapper.NoteImageMapper;
import com.miji.note.mapper.NoteMapper;
import com.miji.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private NoteImageMapper noteImageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result add(AddNoteQO qo) {
        LocalDateTime now = LocalDateTime.now();
        NoteDO noteDO = new NoteDO();
        noteDO.setUserId(qo.getUserId());
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
    public Result delete(DeleteNoteQO qo) {
        int delete = noteMapper.deleteById(qo.getId());
        if (delete <= 0) {
            return Result.fail(CodeEnum.CUSTOM_DATABASE_ERROR_UPDATE_FAIL.getStatusCode(), "note delete fail");
        }
        return Result.success(true);
    }

    @Override
    public Result update(UpdateNoteQO qo) {
        NoteDO noteDO = noteMapper.selectById(qo.getId());
        if (noteDO == null) {
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "note not found");
        }

        if (qo.getUserId() != null) {
            noteDO.setUserId(qo.getUserId());
        }
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
        return Result.success(noteDO);
    }

    @Override
    public Result detail(NoteDetailQO qo) {
        NoteDO noteDO = noteMapper.selectById(qo.getId());
        if (noteDO == null) {
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "note not found");
        }
        return Result.success(noteDO);
    }

    @Override
    public Result list(NoteListQO qo) {
        int page = qo == null || qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
        int size = qo == null || qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();

        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<NoteDO>()
                .eq(NoteDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .orderByDesc(NoteDO::getScore);
        if (qo != null && qo.getUserId() != null) {
            wrapper.eq(NoteDO::getUserId, qo.getUserId());
        }

        return Result.success(noteMapper.selectPage(new Page<>(page, size), wrapper));
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
}
