package com.rex.specdemo.service;

import com.rex.specdemo.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 服務
 * 負責 Token 的產生、驗證與解析
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 產生 JWT Token
     *
     * @param member 會員實體
     * @return JWT Token
     */
    public String generateToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uuid", member.getUuid());
        claims.put("username", member.getUsername());
        return createToken(claims, member.getUsername());
    }

    /**
     * 建立 Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 驗證 Token 是否有效
     *
     * @param token    JWT Token
     * @param username 使用者名稱
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token 驗證失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 從 Token 中取得使用者名稱
     *
     * @param token JWT Token
     * @return 使用者名稱
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 從 Token 中取得 UUID
     *
     * @param token JWT Token
     * @return UUID
     */
    public String extractUuid(String token) {
        return extractClaim(token, claims -> claims.get("uuid", String.class));
    }

    /**
     * 從 Token 中取得過期時間
     *
     * @param token JWT Token
     * @return 過期時間
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 取得 Token 過期時間（秒）
     *
     * @return 過期時間（秒）
     */
    public Long getExpirationInSeconds() {
        return expiration / 1000;
    }

    /**
     * 取得指定的 Claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 取得所有 Claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 檢查 Token 是否過期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 取得簽名金鑰
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
