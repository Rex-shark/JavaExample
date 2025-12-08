package com.rex.jwtdemo.example;

import com.rex.jwtdemo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT Claims 使用範例
 * 展示如何在 Token 中添加和提取自訂資訊
 */
@Service
@RequiredArgsConstructor
public class JwtClaimsExample {

    private final JwtUtil jwtUtil;

    /**
     * 範例 1: 生成帶有使用者資訊的 Token
     */
    public String createUserToken(String username, String role, String email, Long userId) {
        // 建立自訂 claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);           // 角色
        claims.put("email", email);         // Email
        claims.put("userId", userId);       // 使用者 ID
        claims.put("department", "IT");     // 部門

        // 生成 Token
        return jwtUtil.generateToken(username, claims);
    }

    /**
     * 範例 2: 從 Token 提取所有使用者資訊
     */
    public UserInfo extractUserInfo(String token) {
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        String email = jwtUtil.extractEmail(token);

        // 提取自訂的 userId
        Object userIdObj = jwtUtil.extractCustomClaim(token, "userId");
        Long userId = userIdObj != null ? ((Number) userIdObj).longValue() : null;

        // 提取部門資訊
        String department = (String) jwtUtil.extractCustomClaim(token, "department");

        return new UserInfo(username, role, email, userId, department);
    }

    /**
     * 範例 3: 檢查使用者權限
     */
    public boolean hasAdminPermission(String token) {
        String role = jwtUtil.extractRole(token);
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    /**
     * 範例 4: 檢查使用者是否屬於特定部門
     */
    public boolean isFromDepartment(String token, String departmentName) {
        String department = (String) jwtUtil.extractCustomClaim(token, "department");
        return departmentName.equals(department);
    }

    /**
     * 範例 5: 取得所有 Claims 並列印
     */
    public void printAllClaims(String token) {
        Map<String, Object> allClaims = jwtUtil.extractAllClaimsAsMap(token);

        System.out.println("=== Token 中的所有資訊 ===");
        allClaims.forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
    }

    /**
     * 範例 6: 根據角色生成不同的 Token
     */
    public String createTokenByUserType(String username, UserType userType) {
        Map<String, Object> claims = new HashMap<>();

        switch (userType) {
            case ADMIN:
                claims.put("role", "ADMIN");
                claims.put("permissions", new String[]{"READ", "WRITE", "DELETE", "MANAGE_USERS"});
                claims.put("level", 5);
                break;
            case MANAGER:
                claims.put("role", "MANAGER");
                claims.put("permissions", new String[]{"READ", "WRITE", "APPROVE"});
                claims.put("level", 3);
                break;
            case USER:
                claims.put("role", "USER");
                claims.put("permissions", new String[]{"READ"});
                claims.put("level", 1);
                break;
        }

        claims.put("email", username + "@company.com");

        return jwtUtil.generateToken(username, claims);
    }

    /**
     * 範例 7: 驗證使用者是否有特定權限
     */
    public boolean hasPermission(String token, String permission) {
        Object permissionsObj = jwtUtil.extractCustomClaim(token, "permissions");

        if (permissionsObj instanceof String[]) {
            String[] permissions = (String[]) permissionsObj;
            for (String p : permissions) {
                if (p.equals(permission)) {
                    return true;
                }
            }
        }

        return false;
    }

    // ===== 輔助類別 =====

    public static class UserInfo {
        private String username;
        private String role;
        private String email;
        private Long userId;
        private String department;

        public UserInfo(String username, String role, String email, Long userId, String department) {
            this.username = username;
            this.role = role;
            this.email = email;
            this.userId = userId;
            this.department = department;
        }

        // Getters
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getEmail() { return email; }
        public Long getUserId() { return userId; }
        public String getDepartment() { return department; }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "username='" + username + '\'' +
                    ", role='" + role + '\'' +
                    ", email='" + email + '\'' +
                    ", userId=" + userId +
                    ", department='" + department + '\'' +
                    '}';
        }
    }

    public enum UserType {
        ADMIN, MANAGER, USER
    }
}

