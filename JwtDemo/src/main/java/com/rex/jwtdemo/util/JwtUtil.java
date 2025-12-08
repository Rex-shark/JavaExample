package com.rex.jwtdemo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具類
 * 提供 JWT Token 的生成、解析、驗證功能
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJwtTokenGenerationAndValidation12345678}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // 預設 1 小時 (毫秒)
    private Long expiration;

    /**
     * 獲取簽名密鑰
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     * @param username 使用者名稱
     * @return JWT Token
     */
    public String generateToken(String username) {
        return generateToken(username, null);
    }

    /**
     * 生成 JWT Token (帶自訂 claims)
     * @param username 使用者名稱
     * @param claims 自訂的額外資訊
     * @return JWT Token
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 從 Token 中提取使用者名稱
     * @param token JWT Token
     * @return 使用者名稱
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 從 Token 中提取過期時間
     * @param token JWT Token
     * @return 過期時間
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 從 Token 中提取特定 Claim
     * @param token JWT Token
     * @param claimsResolver Claim 解析函數
     * @return 提取的 Claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 從 Token 中提取所有 Claims
     * @param token JWT Token
     * @return 所有 Claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 從 Token 中提取所有 Claims (公開方法)
     * @param token JWT Token
     * @return 所有 Claims 的 Map
     */
    public Map<String, Object> extractAllClaimsAsMap(String token) {
        return extractAllClaims(token);
    }

    /**
     * 從 Token 中提取特定的自訂 Claim
     * @param token JWT Token
     * @param claimKey Claim 的 key
     * @return Claim 的值
     */
    public Object extractCustomClaim(String token, String claimKey) {
        final Claims claims = extractAllClaims(token);
        return claims.get(claimKey);
    }

    /**
     * 從 Token 中提取角色資訊
     * @param token JWT Token
     * @return 角色
     */
    public String extractRole(String token) {
        return (String) extractCustomClaim(token, "role");
    }

    /**
     * 從 Token 中提取 Email
     * @param token JWT Token
     * @return Email
     */
    public String extractEmail(String token) {
        return (String) extractCustomClaim(token, "email");
    }

    /**
     * 檢查 Token 是否過期
     * @param token JWT Token
     * @return 是否過期
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Token 過期檢查失敗: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 驗證 Token 是否有效
     * @param token JWT Token
     * @param username 要驗證的使用者名稱
     * @return Token 是否有效
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token 驗證失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 驗證 Token 是否有效（不驗證使用者名稱）
     * @param token JWT Token
     * @return Token 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token 驗證失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新 Token
     * @param token 舊的 JWT Token
     * @return 新的 JWT Token
     */
    public String refreshToken(String token) {
        final String username = extractUsername(token);
        return generateToken(username);
    }
}

