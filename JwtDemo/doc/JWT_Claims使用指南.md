# JWT Claims 自訂資訊使用指南

## 如何在 Token 中添加自訂資訊

### 1. 生成 Token 時添加 Claims

在 `JwtController.java` 的 login 方法中：

```java
// 生成 Token (可以添加自訂 claims)
Map<String, Object> claims = new HashMap<>();
claims.put("role", "USER");           // 添加角色資訊
claims.put("email", request.getUsername() + "@example.com");  // 添加 Email
claims.put("userId", 12345);          // 可以添加用戶ID
claims.put("department", "IT");       // 可以添加部門
// ... 可以添加任何需要的資訊

String token = jwtUtil.generateToken(request.getUsername(), claims);
```

## 如何從 Token 中取得自訂資訊

### 方法 1: 使用 JwtUtil 的專用方法（推薦）

#### 取得角色資訊
```java
String role = jwtUtil.extractRole(token);
// 結果: "USER"
```

#### 取得 Email
```java
String email = jwtUtil.extractEmail(token);
// 結果: "rex@example.com"
```

#### 取得特定的自訂 Claim
```java
Object userId = jwtUtil.extractCustomClaim(token, "userId");
// 結果: 12345

String department = (String) jwtUtil.extractCustomClaim(token, "department");
// 結果: "IT"
```

#### 取得所有 Claims
```java
Map<String, Object> allClaims = jwtUtil.extractAllClaimsAsMap(token);
// 結果: {
//   "role": "USER",
//   "email": "rex@example.com",
//   "sub": "rex",
//   "iat": 1733146800,
//   "exp": 1733150400
// }
```

### 方法 2: 在 Controller 中使用

```java
@GetMapping("/user-info")
public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
    String token = extractToken(authHeader);
    
    // 提取使用者資訊
    String username = jwtUtil.extractUsername(token);
    String role = jwtUtil.extractRole(token);
    String email = jwtUtil.extractEmail(token);
    
    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("username", username);
    userInfo.put("role", role);
    userInfo.put("email", email);
    
    return ResponseEntity.ok(userInfo);
}
```

### 方法 3: 在 Service 層使用

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final JwtUtil jwtUtil;
    
    public UserInfo getUserInfoFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        String email = jwtUtil.extractEmail(token);
        
        return new UserInfo(username, role, email);
    }
    
    public boolean isAdmin(String token) {
        String role = jwtUtil.extractRole(token);
        return "ADMIN".equals(role);
    }
}
```

## 完整範例：帶權限控制的 API

### 1. 修改 JwtController 添加測試端點

```java
/**
 * 測試取得使用者資訊
 * GET /api/jwt/user-info
 * Header: Authorization: Bearer {token}
 */
@GetMapping("/user-info")
public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
    String token = extractToken(authHeader);
    if (token == null || !jwtUtil.validateToken(token)) {
        return ResponseEntity.status(401).body(Map.of("error", "未授權"));
    }
    
    String username = jwtUtil.extractUsername(token);
    String role = jwtUtil.extractRole(token);
    String email = jwtUtil.extractEmail(token);
    
    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("username", username);
    userInfo.put("role", role);
    userInfo.put("email", email);
    userInfo.put("message", "使用者資訊取得成功");
    
    return ResponseEntity.ok(userInfo);
}

/**
 * 僅限管理員訪問的端點
 * GET /api/jwt/admin-only
 * Header: Authorization: Bearer {token}
 */
@GetMapping("/admin-only")
public ResponseEntity<Map<String, Object>> adminOnly(@RequestHeader("Authorization") String authHeader) {
    String token = extractToken(authHeader);
    if (token == null || !jwtUtil.validateToken(token)) {
        return ResponseEntity.status(401).body(Map.of("error", "未授權"));
    }
    
    String role = jwtUtil.extractRole(token);
    if (!"ADMIN".equals(role)) {
        return ResponseEntity.status(403).body(Map.of("error", "權限不足，需要管理員權限"));
    }
    
    return ResponseEntity.ok(Map.of("message", "歡迎管理員！", "data", "機密資料"));
}
```

### 2. 登入時根據不同用戶設定不同角色

```java
@PostMapping("/login")
public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {
    if (request.getUsername() == null || request.getUsername().isEmpty()) {
        return ResponseEntity.badRequest().build();
    }
    
    // 根據使用者名稱設定不同角色（實際應查詢資料庫）
    String role = "USER";
    if ("admin".equals(request.getUsername())) {
        role = "ADMIN";
    } else if ("manager".equals(request.getUsername())) {
        role = "MANAGER";
    }
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    claims.put("email", request.getUsername() + "@example.com");
    claims.put("loginTime", new Date());
    
    String token = jwtUtil.generateToken(request.getUsername(), claims);
    
    JwtResponse response = new JwtResponse(token, request.getUsername(), expiration);
    return ResponseEntity.ok(response);
}
```

## 測試範例（使用 jwt-test.http）

```http
### 1. 一般使用者登入
POST http://localhost:8080/api/jwt/login
Content-Type: application/json

{
  "username": "rex",
  "password": "123456"
}

### 2. 管理員登入
POST http://localhost:8080/api/jwt/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

### 3. 取得使用者資訊
GET http://localhost:8080/api/jwt/user-info
Authorization: Bearer {your-token-here}

### 4. 訪問管理員專用端點（用一般使用者 token 會被拒絕）
GET http://localhost:8080/api/jwt/admin-only
Authorization: Bearer {user-token-here}

### 5. 訪問管理員專用端點（用管理員 token 可以訪問）
GET http://localhost:8080/api/jwt/admin-only
Authorization: Bearer {admin-token-here}

### 6. 解析 Token 查看所有 Claims
GET http://localhost:8080/api/jwt/parse
Authorization: Bearer {your-token-here}
```

## JwtUtil 新增的方法總覽

| 方法名稱 | 參數 | 返回值 | 說明 |
|---------|------|--------|------|
| `extractCustomClaim(token, key)` | String token, String key | Object | 提取特定的自訂 Claim |
| `extractRole(token)` | String token | String | 提取角色資訊 |
| `extractEmail(token)` | String token | String | 提取 Email |
| `extractAllClaimsAsMap(token)` | String token | Map<String, Object> | 提取所有 Claims 為 Map |

## 常見使用場景

### 1. 權限控制
```java
String role = jwtUtil.extractRole(token);
if (!"ADMIN".equals(role)) {
    throw new ForbiddenException("需要管理員權限");
}
```

### 2. 審計日誌
```java
String username = jwtUtil.extractUsername(token);
String email = jwtUtil.extractEmail(token);
auditLog.record(username, email, "執行了某操作");
```

### 3. 個性化回應
```java
String role = jwtUtil.extractRole(token);
if ("VIP".equals(role)) {
    return getVipData();
} else {
    return getRegularData();
}
```

### 4. 多租戶系統
```java
Map<String, Object> claims = new HashMap<>();
claims.put("tenantId", "tenant-001");
claims.put("role", "USER");
String token = jwtUtil.generateToken(username, claims);

// 使用時
String tenantId = (String) jwtUtil.extractCustomClaim(token, "tenantId");
```

## 注意事項 ⚠️

1. **不要存放敏感資訊**：JWT 的 payload 是 Base64 編碼，不是加密，任何人都可以解碼查看
2. **控制 Token 大小**：Claims 越多，Token 越大，會增加網路傳輸負擔
3. **標準 Claims**：優先使用 JWT 標準的 Claims (sub, iat, exp, iss 等)
4. **型別轉換**：從 Claims 取出的值需要進行適當的型別轉換
5. **空值檢查**：取得 Claim 前應先檢查 Token 有效性，避免 NullPointerException

## 回答主人的問題 💡

**問：這個資訊該如何取得？**

答：有三種方式！

1. **直接用專用方法**（最簡單）：
   ```java
   String role = jwtUtil.extractRole(token);
   String email = jwtUtil.extractEmail(token);
   ```

2. **用通用方法取得特定 Claim**：
   ```java
   String role = (String) jwtUtil.extractCustomClaim(token, "role");
   String email = (String) jwtUtil.extractCustomClaim(token, "email");
   ```

3. **取得所有 Claims 再篩選**：
   ```java
   Map<String, Object> allClaims = jwtUtil.extractAllClaimsAsMap(token);
   String role = (String) allClaims.get("role");
   String email = (String) allClaims.get("email");
   ```

推薦使用第 1 種方式，最清晰且型別安全！

