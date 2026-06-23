package com.miji.core.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.DO.PrivateConversationDO;
import com.common.DO.PrivateMessageDO;
import com.common.DO.UserDO;
import com.common.QO.message.ConversationListQO;
import com.common.QO.message.MessageListQO;
import com.common.QO.message.SendMessageQO;
import com.common.QO.message.StartConversationQO;
import com.common.VO.message.ConversationVO;
import com.common.VO.message.MessageVO;
import com.common.VO.note.NoteAuthorVO;
import com.common.enums.CodeEnum;
import com.common.enums.DefaultValue;
import com.common.exception.CustomException;
import com.common.result.Result;
import com.miji.core.message.mapper.PrivateConversationMapper;
import com.miji.core.message.mapper.PrivateMessageMapper;
import com.miji.core.message.service.MessageService;
import com.miji.core.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
public class MessageServiceImpl implements MessageService {

    @Autowired
    private PrivateConversationMapper conversationMapper;
    @Autowired
    private PrivateMessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO send(SendMessageQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        Long receiverUserId = qo.getReceiverUserId();
        if (currentUserId.equals(receiverUserId)) {
            throw new CustomException(HttpServletResponse.SC_BAD_REQUEST, "can not send message to yourself");
        }
        checkActiveUser(receiverUserId);

        LocalDateTime now = LocalDateTime.now();
        PrivateConversationDO conversation = getOrCreateConversation(currentUserId, receiverUserId, now);

        PrivateMessageDO message = new PrivateMessageDO();
        message.setConversationId(conversation.getId());
        message.setSenderUserId(currentUserId);
        message.setReceiverUserId(receiverUserId);
        message.setContent(qo.getContent().trim());
        message.setIsRead(DefaultValue.NUM_ZERO);
        message.setSenderDeleted(DefaultValue.NUM_ZERO);
        message.setReceiverDeleted(DefaultValue.NUM_ZERO);
        message.setStatus(DefaultValue.DEFAULT_STATUS);
        message.setCreateTime(now);
        message.setUpdateTime(now);
        int insert = messageMapper.insert(message);
        if (insert <= 0) {
            throw new CustomException(CodeEnum.CUSTOM_DATABASE_ERROR_INSERT_FAIL.getStatusCode(), "message insert fail");
        }

        updateConversationAfterSend(conversation, message, now);
        MessageVO vo = buildMessageVO(message);
        pushMessage(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startConversation(StartConversationQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        Long receiverUserId = qo.getReceiverUserId();
        if (currentUserId.equals(receiverUserId)) {
            throw new CustomException(HttpServletResponse.SC_BAD_REQUEST, "can not start conversation with yourself");
        }
        checkActiveUser(receiverUserId);

        PrivateConversationDO conversation = getOrCreateConversation(currentUserId, receiverUserId, LocalDateTime.now());
        if (isConversationDeleted(conversation, currentUserId)) {
            restoreConversation(conversation.getId(), currentUserId);
            conversation = conversationMapper.selectById(conversation.getId());
        }
        UserDO peerUser = userMapper.selectById(receiverUserId);
        return Result.success(buildConversationVO(conversation, currentUserId, peerUser));
    }

    @Override
    public Result conversationList(ConversationListQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        int page = qo == null || qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
        int size = qo == null || qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();

        Page<PrivateConversationDO> conversationPage = conversationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PrivateConversationDO>()
                        .eq(PrivateConversationDO::getStatus, DefaultValue.DEFAULT_STATUS)
                        .and(wrapper -> wrapper
                                .eq(PrivateConversationDO::getUserAId, currentUserId)
                                .eq(PrivateConversationDO::getUserADeleted, DefaultValue.NUM_ZERO)
                                .or()
                                .eq(PrivateConversationDO::getUserBId, currentUserId)
                                .eq(PrivateConversationDO::getUserBDeleted, DefaultValue.NUM_ZERO))
                        .orderByDesc(PrivateConversationDO::getLastMessageTime)
                        .orderByDesc(PrivateConversationDO::getUpdateTime));

        return Result.success(buildConversationPage(conversationPage, currentUserId));
    }

    @Override
    public Result messageList(MessageListQO qo, Long currentUserId) {
        checkLoginUser(currentUserId);
        PrivateConversationDO conversation = checkConversationMember(qo.getConversationId(), currentUserId);
        int page = qo.getPage() == null ? DefaultValue.NUM_PAGE : qo.getPage();
        int size = qo.getSize() == null ? DefaultValue.NUM_SIZE : qo.getSize();

        LambdaQueryWrapper<PrivateMessageDO> wrapper = new LambdaQueryWrapper<PrivateMessageDO>()
                .eq(PrivateMessageDO::getConversationId, conversation.getId())
                .eq(PrivateMessageDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .and(visibleWrapper -> visibleWrapper
                        .eq(PrivateMessageDO::getSenderUserId, currentUserId)
                        .eq(PrivateMessageDO::getSenderDeleted, DefaultValue.NUM_ZERO)
                        .or()
                        .eq(PrivateMessageDO::getReceiverUserId, currentUserId)
                        .eq(PrivateMessageDO::getReceiverDeleted, DefaultValue.NUM_ZERO))
                .orderByDesc(PrivateMessageDO::getCreateTime);

        Page<PrivateMessageDO> messagePage = messageMapper.selectPage(new Page<>(page, size), wrapper);
        Page<MessageVO> result = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        result.setRecords(messagePage.getRecords().stream()
                .map(this::buildMessageVO)
                .collect(Collectors.toList()));
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result read(Long conversationId, Long currentUserId) {
        checkLoginUser(currentUserId);
        PrivateConversationDO conversation = checkConversationMember(conversationId, currentUserId);
        LocalDateTime now = LocalDateTime.now();

        messageMapper.update(null, new LambdaUpdateWrapper<PrivateMessageDO>()
                .eq(PrivateMessageDO::getConversationId, conversationId)
                .eq(PrivateMessageDO::getReceiverUserId, currentUserId)
                .eq(PrivateMessageDO::getIsRead, DefaultValue.NUM_ZERO)
                .eq(PrivateMessageDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .set(PrivateMessageDO::getIsRead, DefaultValue.NUM_ONE)
                .set(PrivateMessageDO::getUpdateTime, now));

        LambdaUpdateWrapper<PrivateConversationDO> updateWrapper = new LambdaUpdateWrapper<PrivateConversationDO>()
                .eq(PrivateConversationDO::getId, conversationId)
                .set(PrivateConversationDO::getUpdateTime, now);
        if (currentUserId.equals(conversation.getUserAId())) {
            updateWrapper.set(PrivateConversationDO::getUserAUnreadCount, DefaultValue.NUM_ZERO);
        } else {
            updateWrapper.set(PrivateConversationDO::getUserBUnreadCount, DefaultValue.NUM_ZERO);
        }
        conversationMapper.update(null, updateWrapper);
        return Result.success(true);
    }

    @Override
    public Result unreadCount(Long currentUserId) {
        checkLoginUser(currentUserId);
        List<PrivateConversationDO> conversations = conversationMapper.selectList(new LambdaQueryWrapper<PrivateConversationDO>()
                .eq(PrivateConversationDO::getStatus, DefaultValue.DEFAULT_STATUS)
                .and(wrapper -> wrapper
                        .eq(PrivateConversationDO::getUserAId, currentUserId)
                        .eq(PrivateConversationDO::getUserADeleted, DefaultValue.NUM_ZERO)
                        .or()
                        .eq(PrivateConversationDO::getUserBId, currentUserId)
                        .eq(PrivateConversationDO::getUserBDeleted, DefaultValue.NUM_ZERO)));
        int count = conversations.stream()
                .mapToInt(conversation -> currentUserId.equals(conversation.getUserAId())
                        ? safeInt(conversation.getUserAUnreadCount())
                        : safeInt(conversation.getUserBUnreadCount()))
                .sum();
        return Result.success(count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteMessage(Long messageId, Long currentUserId) {
        checkLoginUser(currentUserId);
        PrivateMessageDO message = messageMapper.selectById(messageId);
        if (message == null || !DefaultValue.DEFAULT_STATUS.equals(message.getStatus())) {
            return Result.success(true);
        }
        if (!currentUserId.equals(message.getSenderUserId()) && !currentUserId.equals(message.getReceiverUserId())) {
            throw new CustomException(HttpServletResponse.SC_FORBIDDEN, "no permission");
        }

        LambdaUpdateWrapper<PrivateMessageDO> updateWrapper = new LambdaUpdateWrapper<PrivateMessageDO>()
                .eq(PrivateMessageDO::getId, messageId)
                .set(PrivateMessageDO::getUpdateTime, LocalDateTime.now());
        if (currentUserId.equals(message.getSenderUserId())) {
            updateWrapper.set(PrivateMessageDO::getSenderDeleted, DefaultValue.NUM_ONE);
        } else {
            updateWrapper.set(PrivateMessageDO::getReceiverDeleted, DefaultValue.NUM_ONE);
        }
        messageMapper.update(null, updateWrapper);
        return Result.success(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteConversation(Long conversationId, Long currentUserId) {
        checkLoginUser(currentUserId);
        PrivateConversationDO conversation = checkConversationMember(conversationId, currentUserId);
        LambdaUpdateWrapper<PrivateConversationDO> updateWrapper = new LambdaUpdateWrapper<PrivateConversationDO>()
                .eq(PrivateConversationDO::getId, conversationId)
                .set(PrivateConversationDO::getUpdateTime, LocalDateTime.now());
        if (currentUserId.equals(conversation.getUserAId())) {
            updateWrapper.set(PrivateConversationDO::getUserADeleted, DefaultValue.NUM_ONE)
                    .set(PrivateConversationDO::getUserAUnreadCount, DefaultValue.NUM_ZERO);
        } else {
            updateWrapper.set(PrivateConversationDO::getUserBDeleted, DefaultValue.NUM_ONE)
                    .set(PrivateConversationDO::getUserBUnreadCount, DefaultValue.NUM_ZERO);
        }
        conversationMapper.update(null, updateWrapper);
        return Result.success(true);
    }

    private PrivateConversationDO getOrCreateConversation(Long userId, Long otherUserId, LocalDateTime now) {
        Long userAId = Math.min(userId, otherUserId);
        Long userBId = Math.max(userId, otherUserId);
        PrivateConversationDO conversation = findConversation(userAId, userBId);
        if (conversation != null) {
            return conversation;
        }

        PrivateConversationDO newConversation = new PrivateConversationDO();
        newConversation.setUserAId(userAId);
        newConversation.setUserBId(userBId);
        newConversation.setUserAUnreadCount(DefaultValue.NUM_ZERO);
        newConversation.setUserBUnreadCount(DefaultValue.NUM_ZERO);
        newConversation.setUserADeleted(DefaultValue.NUM_ZERO);
        newConversation.setUserBDeleted(DefaultValue.NUM_ZERO);
        newConversation.setStatus(DefaultValue.DEFAULT_STATUS);
        newConversation.setCreateTime(now);
        newConversation.setUpdateTime(now);
        try {
            conversationMapper.insert(newConversation);
            return newConversation;
        } catch (DuplicateKeyException e) {
            return findConversation(userAId, userBId);
        }
    }

    private PrivateConversationDO findConversation(Long userAId, Long userBId) {
        return conversationMapper.selectOne(new LambdaQueryWrapper<PrivateConversationDO>()
                .eq(PrivateConversationDO::getUserAId, userAId)
                .eq(PrivateConversationDO::getUserBId, userBId)
                .last("LIMIT 1"));
    }

    private void updateConversationAfterSend(PrivateConversationDO conversation, PrivateMessageDO message, LocalDateTime now) {
        LambdaUpdateWrapper<PrivateConversationDO> updateWrapper = new LambdaUpdateWrapper<PrivateConversationDO>()
                .eq(PrivateConversationDO::getId, conversation.getId())
                .set(PrivateConversationDO::getLastMessageId, message.getId())
                .set(PrivateConversationDO::getLastMessageContent, trimLastMessage(message.getContent()))
                .set(PrivateConversationDO::getLastMessageTime, now)
                .set(PrivateConversationDO::getUpdateTime, now)
                .set(PrivateConversationDO::getStatus, DefaultValue.DEFAULT_STATUS);

        if (message.getReceiverUserId().equals(conversation.getUserAId())) {
            updateWrapper.setSql("user_a_unread_count = IFNULL(user_a_unread_count, 0) + 1")
                    .set(PrivateConversationDO::getUserADeleted, DefaultValue.NUM_ZERO)
                    .set(PrivateConversationDO::getUserBDeleted, DefaultValue.NUM_ZERO);
        } else {
            updateWrapper.setSql("user_b_unread_count = IFNULL(user_b_unread_count, 0) + 1")
                    .set(PrivateConversationDO::getUserADeleted, DefaultValue.NUM_ZERO)
                    .set(PrivateConversationDO::getUserBDeleted, DefaultValue.NUM_ZERO);
        }
        conversationMapper.update(null, updateWrapper);
    }

    private boolean isConversationDeleted(PrivateConversationDO conversation, Long currentUserId) {
        if (currentUserId.equals(conversation.getUserAId())) {
            return DefaultValue.NUM_ONE.equals(conversation.getUserADeleted());
        }
        return DefaultValue.NUM_ONE.equals(conversation.getUserBDeleted());
    }

    private void restoreConversation(Long conversationId, Long currentUserId) {
        PrivateConversationDO conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return;
        }

        LambdaUpdateWrapper<PrivateConversationDO> updateWrapper = new LambdaUpdateWrapper<PrivateConversationDO>()
                .eq(PrivateConversationDO::getId, conversationId)
                .set(PrivateConversationDO::getUpdateTime, LocalDateTime.now());
        if (currentUserId.equals(conversation.getUserAId())) {
            updateWrapper.set(PrivateConversationDO::getUserADeleted, DefaultValue.NUM_ZERO);
        } else {
            updateWrapper.set(PrivateConversationDO::getUserBDeleted, DefaultValue.NUM_ZERO);
        }
        conversationMapper.update(null, updateWrapper);
    }

    private Page<ConversationVO> buildConversationPage(Page<PrivateConversationDO> conversationPage, Long currentUserId) {
        Page<ConversationVO> result = new Page<>(conversationPage.getCurrent(), conversationPage.getSize(), conversationPage.getTotal());
        List<PrivateConversationDO> records = conversationPage.getRecords();
        if (records == null || records.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        Set<Long> peerUserIds = records.stream()
                .map(conversation -> getPeerUserId(conversation, currentUserId))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserDO> userMap = peerUserIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(peerUserIds).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity(), (first, second) -> first));

        result.setRecords(records.stream()
                .map(conversation -> buildConversationVO(conversation, currentUserId, userMap.get(getPeerUserId(conversation, currentUserId))))
                .collect(Collectors.toList()));
        return result;
    }

    private ConversationVO buildConversationVO(PrivateConversationDO conversation, Long currentUserId, UserDO peerUser) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conversation.getId());
        vo.setPeerUser(buildNoteAuthorVO(peerUser));
        vo.setLastMessageId(conversation.getLastMessageId());
        vo.setLastMessageContent(conversation.getLastMessageContent());
        vo.setLastMessageTime(conversation.getLastMessageTime());
        vo.setUnreadCount(currentUserId.equals(conversation.getUserAId())
                ? safeInt(conversation.getUserAUnreadCount())
                : safeInt(conversation.getUserBUnreadCount()));
        vo.setCreateTime(conversation.getCreateTime());
        vo.setUpdateTime(conversation.getUpdateTime());
        return vo;
    }

    private MessageVO buildMessageVO(PrivateMessageDO message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setSenderUserId(message.getSenderUserId());
        vo.setReceiverUserId(message.getReceiverUserId());
        vo.setContent(message.getContent());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
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

    private PrivateConversationDO checkConversationMember(Long conversationId, Long currentUserId) {
        PrivateConversationDO conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !DefaultValue.DEFAULT_STATUS.equals(conversation.getStatus())) {
            throw new CustomException(HttpServletResponse.SC_NOT_FOUND, "conversation not found");
        }
        if (!currentUserId.equals(conversation.getUserAId()) && !currentUserId.equals(conversation.getUserBId())) {
            throw new CustomException(HttpServletResponse.SC_FORBIDDEN, "no permission");
        }
        return conversation;
    }

    private Long getPeerUserId(PrivateConversationDO conversation, Long currentUserId) {
        if (currentUserId.equals(conversation.getUserAId())) {
            return conversation.getUserBId();
        }
        if (currentUserId.equals(conversation.getUserBId())) {
            return conversation.getUserAId();
        }
        return null;
    }

    private void pushMessage(MessageVO vo) {
        messagingTemplate.convertAndSendToUser(String.valueOf(vo.getReceiverUserId()), "/queue/messages", vo);
        messagingTemplate.convertAndSendToUser(String.valueOf(vo.getSenderUserId()), "/queue/messages", vo);
    }

    private void checkLoginUser(Long currentUserId) {
        if (currentUserId == null) {
            throw new CustomException(HttpServletResponse.SC_UNAUTHORIZED, "please login first");
        }
    }

    private void checkActiveUser(Long userId) {
        UserDO userDO = userMapper.selectById(userId);
        if (userDO == null || !DefaultValue.DEFAULT_STATUS.equals(userDO.getStatus())) {
            throw new CustomException(CodeEnum.COMMON_ERROR.getStatusCode(), "user not found or inactive");
        }
    }

    private String trimLastMessage(String content) {
        if (content == null) {
            return null;
        }
        return content.length() > 200 ? content.substring(0, 200) : content;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
