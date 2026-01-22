package com.medical.research.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类：生成、解析、验证token + 新增token续期功能
 */
@Slf4j
@Component
public class JwtUtil {

    // JWT 签名密钥（建议配置在yml中，长度至少32位）
    @Value("${jwt.secret:abcdefghijklmnopqrstuvwxyz1234567890}")
    private String secret;

    // Token 过期时间（默认4小时，单位：毫秒）
    @Value("${jwt.expiration:14400000}")
    private long expiration;

    // Token 续期阈值（默认1小时，单位：毫秒）：剩余有效期小于该值时触发续期
    @Value("${jwt.renewal-threshold:3600000}")
    private long renewalThreshold;

    // 生成签名密钥
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 根据用户信息生成 Token
     */
    public String generateToken(UserDetails userDetails) {
        // 自定义Claims（可添加用户ID、角色等信息）
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 核心Token生成方法（抽离便于续期复用）
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // 用户名作为Subject
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }

    /**
     * 从Token中解析用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 解析Token中的指定Claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析Token的所有Claims
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析JWT Token失败", e);
            throw new JwtException("无效的Token");
        }
    }

    /**
     * 验证Token是否有效（用户名匹配 + 未过期）
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 检查Token是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 解析Token过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ======================== 新增Token续期相关方法 ========================

    /**
     * 判断Token是否需要续期（剩余有效期 < 续期阈值 且 Token未过期）
     */
    public boolean isTokenNeedRenewal(String token) {
        if (isTokenExpired(token)) {
            log.warn("Token已过期，无法续期");
            return false;
        }
        // 计算Token剩余有效期（毫秒）
        long remainingTime = extractExpiration(token).getTime() - System.currentTimeMillis();
        log.info("Token剩余有效期：{} 毫秒，续期阈值：{} 毫秒", remainingTime, renewalThreshold);
        // 剩余时间小于续期阈值时，需要续期
        return remainingTime < renewalThreshold;
    }

    /**
     * Token续期：生成新的Token（保留原用户名，重新计算过期时间）
     * @param token 原有效Token
     * @return 新的Token
     * @throws JwtException 原Token无效/过期时抛出异常
     */
    public String renewToken(String token) {
        // 1. 校验原Token是否有效（避免给过期/无效Token续期）
        if (isTokenExpired(token)) {
            throw new JwtException("Token已过期，无法续期");
        }
        // 2. 提取原Token的用户名
        String username = extractUsername(token);
        // 3. 生成新的Token（复用生成逻辑，过期时间重新计算）
        Map<String, Object> claims = new HashMap<>();
        String newToken = createToken(claims, username);
        log.info("Token续期成功，原用户名：{}", username);
        return newToken;
    }

    /**
     * 重载：根据UserDetails直接续期（适用于已获取用户信息的场景）
     */
    public String renewToken(UserDetails userDetails) {
        log.info("为用户 {} 生成续期Token", userDetails.getUsername());
        return generateToken(userDetails);
    }
}