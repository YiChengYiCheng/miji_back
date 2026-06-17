package com.miji.core.message.controller;

import com.common.QO.message.SendMessageQO;
import com.miji.core.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.security.Principal;

@Controller
public class MessageWsController {

    @Autowired
    private MessageService messageService;

    @MessageMapping("/message/send")
    public void send(@Payload @Valid SendMessageQO qo, Principal principal) {
        messageService.send(qo, Long.valueOf(principal.getName()));
    }
}
