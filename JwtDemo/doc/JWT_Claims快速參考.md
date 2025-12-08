# JWT Claims 快速參考卡 🚀

## 📝 如何添加自訂資訊到 Token

```java
// 建立 claims Map
Map<String, Object> claims = new HashMap<>();
claims.put("role", "USER");           // 添加角色
claims.put("email", "rex@example.com"); // 添加 email
claims.put("userId", 12345);          // 添加使用者 ID
claims.put("department", "IT");       // 添加部門

// 生成帶 claims 的 Token
String token = jwtUtil.generateToken(username, claims);
```

## 📖 如何從 Token 取得資訊

### 方法 1️⃣：使用專用方法（最簡單）
```java
String role = jwtUtil.extractRole(token);        // 取得角色
String email = jwtUtil.extractEmail(token);      // 取得 email
```

### 方法 2️⃣：使用通用方法
```java
String role = (String) jwtUtil.extractCustomClaim(token, "role");
Long userId = ((Number) jwtUtil.extractCustomClaim(token, "userId")).longValue();
String department = (String) jwtUtil.extractCustomClaim(token, "department");
```

### 方法 3️⃣：取得所有 Claims
```java
Map<String, Object> allClaims = jwtUtil.extractAllClaimsAsMap(token);
String role = (String) allClaims.get("role");
String email = (String) allClaims.get("email");
// ... 依此類推
```

## 🎯 常見使用場景

### ✅ 權限檢查
```java
String role = jwtUtil.extractRole(token);
if ("ADMIN".equals(role)) {
    // 執行管理員操作
}
```

### ✅ 在 Controller 中使用
```java
@GetMapping("/user-info")
public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String auth) {
    String token = auth.substring(7); // 移除 "Bearer "
    
    String username = jwtUtil.extractUsername(token);
    String role = jwtUtil.extractRole(token);
    String email = jwtUtil.extractEmail(token);
    
    return ResponseEntity.ok(Map.of(
        "username", username,
        "role", role,
        "email", email
    ));
}
```

### ✅ 在 Service 中使用
```java
@Service
public class UserService {
    @Autowired
    private JwtUtil jwtUtil;
    
    public boolean isAdmin(String token) {
        String role = jwtUtil.extractRole(token);
        return "ADMIN".equals(role);
    }
}
```

## 📊 JwtUtil 可用方法

| 方法 | 說明 | 範例 |
|------|------|------|
| `extractUsername(token)` | 提取使用者名稱 | `String name = jwtUtil.extractUsername(token);` |
| `extractRole(token)` | 提取角色 | `String role = jwtUtil.extractRole(token);` |
| `extractEmail(token)` | 提取 email | `String email = jwtUtil.extractEmail(token);` |
| `extractCustomClaim(token, key)` | 提取自訂 claim | `Object value = jwtUtil.extractCustomClaim(token, "userId");` |
| `extractAllClaimsAsMap(token)` | 提取所有 claims | `Map<String, Object> claims = jwtUtil.extractAllClaimsAsMap(token);` |
| `extractExpiration(token)` | 提取過期時間 | `Date exp = jwtUtil.extractExpiration(token);` |
| `validateToken(token)` | 驗證 token | `boolean valid = jwtUtil.validateToken(token);` |
| `isTokenExpired(token)` | 檢查是否過期 | `boolean expired = jwtUtil.isTokenExpired(token);` |
| `refreshToken(token)` | 刷新 token | `String newToken = jwtUtil.refreshToken(token);` |

## 🧪 測試流程

### Step 1: 登入取得 Token
```http
POST http://localhost:8080/api/jwt/login
Content-Type: application/json

{"username": "rex", "password": "123456"}
```

### Step 2: 複製回應中的 token

### Step 3: 解析 Token 查看資訊
```http
GET http://localhost:8080/api/jwt/parse
Authorization: Bearer {貼上你的token}
```

### Step 4: 查看回應
```json
{
  "username": "rex",
  "role": "USER",              ← 你添加的 role
  "email": "rex@example.com",  ← 你添加的 email
  "expired": false,
  "expirationDate": "2025-12-02T15:30:00",
  "allClaims": {
    "role": "USER",
    "email": "rex@example.com",
    "sub": "rex",
    "iat": 1733146800,
    "exp": 1733150400
  }
}
```

## ⚠️ 重要提醒

1. **JWT 不加密**：payload 只是 Base64 編碼，不要存放密碼等敏感資訊
2. **控制大小**：claims 越多，token 越大
3. **型別轉換**：從 claims 取出的值需要進行型別轉換
4. **空值檢查**：使用前先驗證 token 有效性

## 💡 完整範例

```java
// 在 Controller 中
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyController {
    
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 驗證使用者（這裡簡化）
        
        // 建立 claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        claims.put("email", request.getUsername() + "@example.com");
        
        // 生成 token
        String token = jwtUtil.generateToken(request.getUsername(), claims);
        
        return ResponseEntity.ok(Map.of("token", token));
    }
    
    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint(@RequestHeader("Authorization") String auth) {
        String token = auth.substring(7); // 移除 "Bearer "
        
        // 驗證 token
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("Token 無效");
        }
        
        // 提取資訊
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        
        // 權限檢查
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("權限不足");
        }
        
        return ResponseEntity.ok("歡迎管理員 " + username);
    }
}
```

---

📚 詳細文檔請參考：`JWT_Claims使用指南.md`  
💻 程式碼範例請參考：`JwtClaimsExample.java`  
🧪 測試文件請參考：`jwt-test.http`

