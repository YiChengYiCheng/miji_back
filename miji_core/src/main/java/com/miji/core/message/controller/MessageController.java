package com.miji.core.message.controller;

import com.common.QO.message.ConversationListQO;
import com.common.QO.message.DeleteConversationQO;
import com.common.QO.message.DeleteMessageQO;
import com.common.QO.message.MessageListQO;
import com.common.QO.message.ReadConversationQO;
import com.common.QO.message.SendMessageQO;
import com.common.QO.message.StartConversationQO;
import com.common.result.Result;
import com.miji.core.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Result send(@RequestBody @Valid SendMessageQO qo, HttpServletRequest request) {
        return Result.success(messageService.send(qo, getUserId(request)));
    }

    @PostMapping("/conversation/start")
    public Result startConversation(@RequestBody @Valid StartConversationQO qo, HttpServletRequest request) {
        return messageService.startConversation(qo, getUserId(request));
    }

    @PostMapping("/conversation/list")
    public Result conversationList(@RequestBody(required = false) ConversationListQO qo, HttpServletRequest request) {
        return messageService.conversationList(qo, getUserId(request));
    }

    @PostMapping("/list")
    public Result messageList(@RequestBody @Valid MessageListQO qo, HttpServletRequest request) {
        return messageService.messageList(qo, getUserId(request));
    }

    @PostMapping("/read")
    public Result read(@RequestBody @Valid ReadConversationQO qo, HttpServletRequest request) {
        return messageService.read(qo.getConversationId(), getUserId(request));
    }

    @PostMapping("/unread/count")
    public Result unreadCount(HttpServletRequest request) {
        return messageService.unreadCount(getUserId(request));
    }

    @PostMapping("/delete/message")
    public Result deleteMessage(@RequestBody @Valid DeleteMessageQO qo, HttpServletRequest request) {
        return messageService.deleteMessage(qo.getMessageId(), getUserId(request));
    }

    @PostMapping("/delete/conversation")
    public Result deleteConversation(@RequestBody @Valid DeleteConversationQO qo, HttpServletRequest request) {
        return messageService.deleteConversation(qo.getConversationId(), getUserId(request));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
