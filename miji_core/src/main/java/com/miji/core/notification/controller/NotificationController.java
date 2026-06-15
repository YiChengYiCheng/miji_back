package com.miji.core.notification.controller;

import com.common.QO.notification.NotificationListQO;
import com.common.QO.notification.ReadNotificationQO;
import com.common.result.Result;
import com.miji.core.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/list")
    public Result list(@RequestBody(required = false) NotificationListQO qo, HttpServletRequest request) {
        return notificationService.list(qo, getUserId(request));
    }

    @PostMapping("/unread/count")
    public Result unreadCount(HttpServletRequest request) {
        return notificationService.unreadCount(getUserId(request));
    }

    @PostMapping("/read")
    public Result read(@RequestBody @Valid ReadNotificationQO qo, HttpServletRequest request) {
        return notificationService.read(qo.getId(), getUserId(request));
    }

    @PostMapping("/read/all")
    public Result readAll(HttpServletRequest request) {
        return notificationService.readAll(getUserId(request));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
