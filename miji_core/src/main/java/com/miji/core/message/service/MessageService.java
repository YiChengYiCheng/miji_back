package com.miji.core.message.service;

import com.common.QO.message.ConversationListQO;
import com.common.QO.message.MessageListQO;
import com.common.QO.message.SendMessageQO;
import com.common.QO.message.StartConversationQO;
import com.common.VO.message.MessageVO;
import com.common.result.Result;

public interface MessageService {

    MessageVO send(SendMessageQO qo, Long currentUserId);

    Result startConversation(StartConversationQO qo, Long currentUserId);

    Result conversationList(ConversationListQO qo, Long currentUserId);

    Result messageList(MessageListQO qo, Long currentUserId);

    Result read(Long conversationId, Long currentUserId);

    Result unreadCount(Long currentUserId);

    Result deleteMessage(Long messageId, Long currentUserId);

    Result deleteConversation(Long conversationId, Long currentUserId);
}
