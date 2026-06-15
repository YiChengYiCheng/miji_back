package com.miji.core.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.DO.CommentDO;
import com.common.DO.NoteDO;
import com.common.DO.NotificationDO;
import com.common.DO.UserDO;
import com.common.QO.notification.NotificationListQO;
import com.common.VO.note.NoteAuthorVO;
import com.common.VO.notification.NotificationVO;
import com.common.enums.DefaultValue;
import com.common.enums.NotificationTypeEnum;
import com.common.exception.CustomException;
import com.common.result.Result;
import com.miji.core.notification.mapper.NotificationMapper;
import com.miji.core.notification.service.NotificationService;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void notifyLike(Long senderUserId, NoteDO noteDO) {
        if (noteDO == null) {
            return;
        }
        createNotification(noteDO.getUserId(), senderUserId, NotificationTypeEnum.LIKE,
                noteDO.getId(), null, NotificationTypeEnum.LIKE.getContent());
    }

    @Override
    public void notifyFollow(Long senderUserId, Long receiverUserId) {
        createNotification(receiverUserId, senderUserId, NotificationTypeEnum.FOLLOW,
                null, null, NotificationTypeEnum.FOLLOW.getContent());
    }

    @Override
    public void notifyComment(Long senderUserId, NoteDO noteDO, CommentDO commentDO) {
        if (noteDO == null || commentDO == null) {
            return;
        }
        createNotification(noteDO.getUserId(), senderUserId, NotificationTypeEnum.COMMENT,
                noteDO.getId(), commentDO.getId(), buildCommentContent(NotificationTypeEnum.COMMENT, commentDO));
    }

    @Override
    public void notifyReply(Long senderUserId, Long receiverUserId, NoteDO noteDO, CommentDO commentDO) {
        if (noteDO == null || commentDO == null) {
            return;
        }
        createNotification(receiverUserId, senderUserId, NotificationTypeEnum.REPLY,
                noteDO.getId(), commentDO.getId(), buildCommentContent(NotificationTypeEnum.REPLY, commentDO));
    }

    @Override
    public Result list(NotificationListQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        int page = qo == null || qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
        int size = qo == null || qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();

        LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<NotificationDO>()
                .eq(NotificationDO::getReceiverUserId, currentUserId)
                .eq(NotificationDO::getStatus, DefaultValue.DEFAULT_STATUS);
        if (qo != null && qo.getType() != null) {
            wrapper.eq(NotificationDO::getType, qo.getType());
        }
        if (qo != null && qo.getIsRead() != null) {
            wrapper.eq(NotificationDO::getIsRead, qo.getIsRead());
        }
        wrapper.orderByDesc(NotificationDO::getCreateTime);

        Page<NotificationDO> notificationPage = notificationMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(buildNotificationPage(notificationPage));
    }

    @Override
    public Result unreadCount(Long currentUserId) {
        checkLoginUser(currentUserId);
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<NotificationDO>()
                .eq(NotificationDO::getReceiverUserId, currentUserId)
                .eq(NotificationDO::getIsRead, DefaultValue.NUM_ZERO)
                .eq(NotificationDO::getStatus, DefaultValue.DEFAULT_STATUS));
        return Result.success(count == null ? 0L : count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result read(Long notificationId, Long currentUserId) {
        checkLoginUser(currentUserId);
        notificationMapper.update(null, new LambdaUpdateWrapper<NotificationDO>()
                .eq(NotificationDO::getId, notificationId)
                .eq(NotificationDO::getReceiverUserId, currentUserId)
                .eq(NotificationDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .set(NotificationDO::getIsRead, DefaultValue.NUM_ONE)
                .set(NotificationDO::getUpdateTime, LocalDateTime.now()));
        return Result.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result readAll(Long currentUserId) {
        checkLoginUser(currentUserId);
        notificationMapper.update(null, new LambdaUpdateWrapper<NotificationDO>()
                .eq(NotificationDO::getReceiverUserId, currentUserId)
                .eq(NotificationDO::getIsRead, DefaultValue.NUM_ZERO)
                .eq(NotificationDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .set(NotificationDO::getIsRead, DefaultValue.NUM_ONE)
                .set(NotificationDO::getUpdateTime, LocalDateTime.now()));
        return Result.success(true);
    }

    private void createNotification(Long receiverUserId, Long senderUserId, NotificationTypeEnum type,
                                    Long noteId, Long commentId, String content) {
        if (receiverUserId == null || senderUserId == null || receiverUserId.equals(senderUserId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        NotificationDO notification = new NotificationDO();
        notification.setReceiverUserId(receiverUserId);
        notification.setSenderUserId(senderUserId);
        notification.setType(type.getType());
        notification.setNoteId(noteId);
        notification.setCommentId(commentId);
        notification.setContent(content);
        notification.setIsRead(DefaultValue.NUM_ZERO);
        notification.setStatus(DefaultValue.DEFAULT_STATUS);
        notification.setCreateTime(now);
        notification.setUpdateTime(now);

        try {
            notificationMapper.insert(notification);
        } catch (DuplicateKeyException ignored) {
            // Unique indexes can be used to suppress repeated like/follow notifications.
        }
    }

    private String buildCommentContent(NotificationTypeEnum type, CommentDO commentDO) {
        String content = commentDO.getContent();
        if (content == null || content.trim().isEmpty()) {
            return type.getContent();
        }
        content = content.trim();
        if (content.length() > 50) {
            content = content.substring(0, 50);
        }
        return type.getContent() + "：" + content;
    }

    private Page<NotificationVO> buildNotificationPage(Page<NotificationDO> notificationPage) {
        Page<NotificationVO> result = new Page<>(notificationPage.getCurrent(), notificationPage.getSize(), notificationPage.getTotal());
        List<NotificationDO> records = notificationPage.getRecords();
        if (records == null || records.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        Set<Long> senderUserIds = records.stream()
                .map(NotificationDO::getSenderUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserDO> userMap = senderUserIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(senderUserIds).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity(), (first, second) -> first));

        result.setRecords(records.stream()
                .map(notification -> buildNotificationVO(notification, userMap.get(notification.getSenderUserId())))
                .collect(Collectors.toList()));
        return result;
    }

    private NotificationVO buildNotificationVO(NotificationDO notificationDO, UserDO sender) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notificationDO.getId());
        vo.setType(notificationDO.getType());
        vo.setNoteId(notificationDO.getNoteId());
        vo.setCommentId(notificationDO.getCommentId());
        vo.setContent(notificationDO.getContent());
        vo.setIsRead(notificationDO.getIsRead());
        vo.setCreateTime(notificationDO.getCreateTime());
        vo.setSender(buildNoteAuthorVO(sender));
        return vo;
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

    private void checkLoginUser(Long currentUserId) {
        if (currentUserId == null) {
            throw new CustomException(HttpServletResponse.SC_UNAUTHORIZED, "please login first");
        }
    }
}
