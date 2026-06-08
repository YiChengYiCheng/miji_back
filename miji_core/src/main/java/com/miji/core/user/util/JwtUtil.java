package com.miji.core.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String TOKEN_TYPE = "type";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";
    private static final String ACCOUNT = "account";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public String createAccessToken(Long userId, String account) {
        return createToken(userId, account, ACCESS_TOKEN, accessTokenExpiration);
    }

    public String createRefreshToken(Long userId, String account) {
        return createToken(userId, account, REFRESH_TOKEN, refreshTokenExpiration);
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isRefreshToken(Claims claims) {
        return REFRESH_TOKEN.equals(claims.get(TOKEN_TYPE, String.class));
    }

    public boolean isAccessToken(Claims claims) {
        return ACCESS_TOKEN.equals(claims.get(TOKEN_TYPE, String.class));
    }

    public Long getAccessTokenExpiresIn() {
        return accessTokenExpiration / 1000;
    }

    public String getAccount(Claims claims) {
        return claims.get(ACCOUNT, String.class);
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    private String createToken(Long userId, String account, String tokenType, Long expiration) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(ACCOUNT, account)
                .claim(TOKEN_TYPE, tokenType)
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(SignatureAlgorithm.HS256,getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
