package com.example.aiplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT令牌提供者
 * <p>
 * 负责JWT令牌的生成、解析和验证。
 * 使用HMAC-SHA算法对令牌进行签名，支持访问令牌和刷新令牌两种类型。
 * 密钥和过期时间通过配置文件注入。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** HMAC签名密钥 */
    private final SecretKey secretKey;
    /** 访问令牌过期时间（毫秒） */
    private final long expiration;
    /** 刷新令牌过期时间（毫秒） */
    private final long refreshExpiration;

    /**
     * 构造函数，从配置文件中读取JWT相关参数
     *
     * @param secret            JWT签名密钥字符串
     * @param expiration        访问令牌过期时间（毫秒）
     * @param refreshExpiration 刷新令牌过期时间（毫秒）
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration) {
        // 将字符串密钥转换为HMAC-SHA密钥对象
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * 根据认证信息生成JWT访问令牌
     *
     * @param authentication Spring Security认证对象
     * @return JWT令牌字符串
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 根据认证信息生成JWT刷新令牌
     * <p>
     * 刷新令牌的有效期更长，并包含 type=refresh 的自定义声明以区分访问令牌。
     * </p>
     *
     * @param authentication Spring Security认证对象
     * @return JWT刷新令牌字符串
     */
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("type", "refresh")
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从JWT令牌中解析用户名
     *
     * @param token JWT令牌字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * 验证JWT令牌是否有效
     * <p>
     * 检查令牌签名、格式、过期时间等，任何异常都会记录日志并返回false。
     * </p>
     *
     * @param token JWT令牌字符串
     * @return 有效返回true，否则返回false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取访问令牌的过期时间
     *
     * @return 过期时间（毫秒）
     */
    public long getExpiration() {
        return expiration;
    }
}
