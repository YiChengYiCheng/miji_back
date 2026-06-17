package com.miji.websocket;

import com.miji.core.user.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("please login first");
        }

        try {
            Claims claims = jwtUtil.parseToken(authorization.substring(BEARER_PREFIX.length()));
            if (!jwtUtil.isAccessToken(claims)) {
                throw new MessagingException("token type error");
            }
            accessor.setUser(new WebSocketUserPrincipal(String.valueOf(jwtUtil.getUserId(claims))));
            return message;
        } catch (JwtException | IllegalArgumentException e) {
            throw new MessagingException("token invalid or expired", e);
        }
    }

    private static class WebSocketUserPrincipal implements Principal {
        private final String name;

        private WebSocketUserPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
