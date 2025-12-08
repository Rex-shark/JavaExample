package com.rex.jwtdemo.controller;

import com.rex.jwtdemo.dto.JwtRequest;
import com.rex.jwtdemo.dto.JwtResponse;
import com.rex.jwtdemo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT 測試 Controller
 * 提供 JWT Token 的生成、驗證、刷新等功能
 */
@RestController
@RequestMapping("/api/jwt")
@RequiredArgsConstructor
public class JwtController {

    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 登入並生成 Token
     * POST /api/jwt/login
     * Body: {"username": "rex", "password": "123456"}
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {
        // 這裡簡化處理，實際專案中應該要驗證密碼
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 生成 Token (可以添加自訂 claims)
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        claims.put("email", request.getUsername() + "@example.com");

        String token = jwtUtil.generateToken(request.getUsername(), claims);

        JwtResponse response = new JwtResponse(
                token,
                request.getUsername(),
                expiration
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 驗證 Token
     * POST /api/jwt/validate
     * Header: Authorization: Bearer {token}
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "缺少 Token"));
        }

        boolean isValid = jwtUtil.validateToken(token);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        if (isValid) {
            String username = jwtUtil.extractUsername(token);
            response.put("username", username);
            response.put("message", "Token 有效");
        } else {
            response.put("message", "Token 無效或已過期");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 刷新 Token
     * POST /api/jwt/refresh
     * Header: Authorization: Bearer {token}
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String username = jwtUtil.extractUsername(token);
            String newToken = jwtUtil.refreshToken(token);

            JwtResponse response = new JwtResponse(
                    newToken,
                    username,
                    expiration
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 解析 Token 內容
     * GET /api/jwt/parse
     * Header: Authorization: Bearer {token}
     */
    @GetMapping("/parse")
    public ResponseEntity<Map<String, Object>> parseToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "缺少 Token"));
        }

        try {
            String username = jwtUtil.extractUsername(token);
            boolean isExpired = jwtUtil.isTokenExpired(token);

            // 提取自訂的 claims
            String role = jwtUtil.extractRole(token);
            String email = jwtUtil.extractEmail(token);

            // 或者取得所有 claims
            Map<String, Object> allClaims = jwtUtil.extractAllClaimsAsMap(token);

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("expired", isExpired);
            response.put("expirationDate", jwtUtil.extractExpiration(token));
            response.put("role", role);
            response.put("email", email);
            response.put("allClaims", allClaims);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token 解析失敗: " + e.getMessage()));
        }
    }

    /**
     * 從 Authorization Header 中提取 Token
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

