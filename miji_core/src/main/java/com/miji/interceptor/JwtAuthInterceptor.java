package com.miji.interceptor;

import com.common.enums.CodeEnum;
import com.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miji.annotation.OptionalLogin;
import com.miji.core.user.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        boolean optionalLogin = isOptionalLogin(handler);
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            if (optionalLogin) {
                return true;
            }
            writeUnauthorized(response, "请先登录！");
            return false;
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtUtil.parseToken(token);
            if (!jwtUtil.isAccessToken(claims)) {
                if (optionalLogin) {
                    return true;
                }
                writeUnauthorized(response, "token类型错误！");
                return false;
            }

            request.setAttribute("userId", jwtUtil.getUserId(claims));
            request.setAttribute("account", jwtUtil.getAccount(claims));
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            if (optionalLogin) {
                return true;
            }
            writeUnauthorized(response, "token无效或已过期！");
            return false;
        }
    }

    private boolean isOptionalLogin(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return handlerMethod.hasMethodAnnotation(OptionalLogin.class)
                || handlerMethod.getBeanType().isAnnotationPresent(OptionalLogin.class);
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), msg)
        ));
    }
}
