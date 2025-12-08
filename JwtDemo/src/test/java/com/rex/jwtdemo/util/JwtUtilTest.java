package com.rex.jwtdemo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具類測試
 */
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private String testUsername;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUsername = "testUser";
        testToken = jwtUtil.generateToken(testUsername);
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken("rex");
        assertNotNull(token);
        assertFalse(token.isEmpty());
        System.out.println("生成的 Token: " + token);
    }

    @Test
    void testGenerateTokenWithClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("email", "rex@example.com");

        String token = jwtUtil.generateToken("rex", claims);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        System.out.println("帶 Claims 的 Token: " + token);
    }

    @Test
    void testExtractUsername() {
        String username = jwtUtil.extractUsername(testToken);
        assertEquals(testUsername, username);
        System.out.println("提取的使用者名稱: " + username);
    }

    @Test
    void testExtractExpiration() {
        var expiration = jwtUtil.extractExpiration(testToken);
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
        System.out.println("過期時間: " + expiration);
    }

    @Test
    void testIsTokenExpired() {
        boolean isExpired = jwtUtil.isTokenExpired(testToken);
        assertFalse(isExpired);
        System.out.println("Token 是否過期: " + isExpired);
    }

    @Test
    void testValidateToken() {
        boolean isValid = jwtUtil.validateToken(testToken, testUsername);
        assertTrue(isValid);
        System.out.println("Token 驗證結果: " + isValid);
    }

    @Test
    void testValidateTokenWithWrongUsername() {
        boolean isValid = jwtUtil.validateToken(testToken, "wrongUser");
        assertFalse(isValid);
        System.out.println("錯誤使用者名稱驗證結果: " + isValid);
    }

    @Test
    void testRefreshToken() {
        String newToken = jwtUtil.refreshToken(testToken);
        assertNotNull(newToken);
        assertNotEquals(testToken, newToken);
        System.out.println("原始 Token: " + testToken);
        System.out.println("刷新後 Token: " + newToken);
    }
}

