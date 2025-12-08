# JWT Demo - JJWT 實作範例

## 專案說明

使用 JJWT 庫實作的 JWT (JSON Web Token) 工具，提供 Token 的生成、驗證、刷新等功能。

## 技術棧

- Spring Boot 4.0.0
- Java 17
- JJWT 0.12.6
- Lombok

## 主要功能

### JwtUtil 工具類

位於 `com.rex.jwtdemo.util.JwtUtil`

#### 主要方法：

1. **生成 Token**
   - `generateToken(String username)` - 基本生成
   - `generateToken(String username, Map<String, Object> claims)` - 帶自訂資訊

2. **解析 Token**
   - `extractUsername(String token)` - 提取使用者名稱
   - `extractExpiration(String token)` - 提取過期時間
   - `extractClaim(String token, Function<Claims, T> claimsResolver)` - 提取特定 Claim

3. **驗證 Token**
   - `validateToken(String token, String username)` - 驗證 Token 與使用者
   - `validateToken(String token)` - 驗證 Token 有效性
   - `isTokenExpired(String token)` - 檢查是否過期

4. **刷新 Token**
   - `refreshToken(String token)` - 生成新的 Token

## API 端點

### 1. 登入取得 Token
```http
POST http://localhost:8080/api/jwt/login
Content-Type: application/json

{
  "username": "rex",
  "password": "123456"
}
```

**回應範例：**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "rex",
  "expiresIn": 3600000
}
```

### 2. 驗證 Token
```http
POST http://localhost:8080/api/jwt/validate
Authorization: Bearer {your-token}
```

**回應範例：**
```json
{
  "valid": true,
  "username": "rex",
  "message": "Token 有效"
}
```

### 3. 刷新 Token
```http
POST http://localhost:8080/api/jwt/refresh
Authorization: Bearer {your-token}
```

**回應範例：**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "rex",
  "expiresIn": 3600000
}
```

### 4. 解析 Token
```http
GET http://localhost:8080/api/jwt/parse
Authorization: Bearer {your-token}
```

**回應範例：**
```json
{
  "username": "rex",
  "expired": false,
  "expirationDate": "2025-12-02T15:30:45.123+00:00"
}
```

## 配置說明

在 `application.properties` 中配置：

```properties
# JWT 密鑰 (至少 32 字元)
jwt.secret=mySecretKeyForJwtTokenGenerationAndValidation12345678

# Token 有效期 (毫秒)
# 3600000 = 1 小時
# 86400000 = 1 天
jwt.expiration=3600000

# 伺服器埠號
server.port=8080
```

## 使用範例

### 在程式碼中使用

```java
@RestController
@RequiredArgsConstructor
public class MyController {
    
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public String login(@RequestParam String username) {
        // 生成 Token
        String token = jwtUtil.generateToken(username);
        return token;
    }
    
    @GetMapping("/protected")
    public String protectedEndpoint(@RequestHeader("Authorization") String auth) {
        String token = auth.substring(7); // 移除 "Bearer "
        
        // 驗證 Token
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            return "歡迎, " + username + "!";
        }
        
        return "Token 無效";
    }
}
```

## 測試

執行單元測試：

```bash
mvn test
```

或在 IDE 中執行 `JwtUtilTest` 類別。

## 注意事項

1. **密鑰安全性**：實際部署時請使用環境變數或配置中心管理密鑰
2. **HTTPS**：生產環境請使用 HTTPS 傳輸 Token
3. **Token 儲存**：前端應安全地儲存 Token（建議使用 HttpOnly Cookie）
4. **過期處理**：Token 過期後需要重新登入或使用刷新機制
5. **密鑰長度**：HMAC-SHA256 需要至少 256 位元（32 字元）的密鑰

## 啟動專案

```bash
# 編譯專案
mvn clean install

# 啟動應用
mvn spring-boot:run
```

應用將在 http://localhost:8080 啟動

## 使用 IntelliJ HTTP Client 測試

創建 `jwt-test.http` 檔案：

```http
### 1. 登入取得 Token
POST http://localhost:8080/api/jwt/login
Content-Type: application/json

{
  "username": "rex",
  "password": "123456"
}

### 2. 驗證 Token (需要先執行上面的登入，複製 token)
POST http://localhost:8080/api/jwt/validate
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

### 3. 解析 Token
GET http://localhost:8080/api/jwt/parse
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

### 4. 刷新 Token
POST http://localhost:8080/api/jwt/refresh
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## License

MIT License

