package com.github.listen_to_me.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtils {

    private static SecretKey KEY;
    private static Long EXPIRATION;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtils.KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Value("${jwt.expiration}")
    public void setExpiration(Long expiration) {
        JwtUtils.EXPIRATION = expiration;
    }

    /**
     * 生成 JWT 令牌
     */
    public static String generateToken(String userId, String username) {
        log.info("生成令牌 - 用户ID: {}, 用户名: {}", userId, username);
        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    /**
     * 验证并解析 JWT 令牌
     */
    public static Claims parseToken(String token) {
        log.debug("解析令牌 - 详情: {}", token);
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证令牌是否有效
     */
    public static boolean validateToken(String token) {
        // TODO: 验证用到了 parseToken, 如果成功需要再解析一遍令牌，非常多余，后续重构
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("令牌验证失败 - 原因: {}", e.getMessage());
            return false;
        }
    }
}
