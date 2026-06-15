package com.miji.core.notification.service;

import com.common.DO.CommentDO;
import com.common.DO.NoteDO;
import com.common.QO.notification.NotificationListQO;
import com.common.result.Result;

public interface NotificationService {

    void notifyLike(Long senderUserId, NoteDO noteDO);

    void notifyFollow(Long senderUserId, Long receiverUserId);

    void notifyComment(Long senderUserId, NoteDO noteDO, CommentDO commentDO);

    void notifyReply(Long senderUserId, Long receiverUserId, NoteDO noteDO, CommentDO commentDO);

    Result list(NotificationListQO qo, Long currentUserId);

    Result unreadCount(Long currentUserId);

    Result read(Long notificationId, Long currentUserId);

    Result readAll(Long currentUserId);
}
