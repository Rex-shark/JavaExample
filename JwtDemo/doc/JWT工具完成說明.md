# JWT 工具完成說明

## 已完成的工作

### 1. 依賴配置
在 `pom.xml` 中添加了：
- Spring Web (用於 REST API)
- JJWT 0.12.6 (最新版本的 JWT 庫)
  - jjwt-api
  - jjwt-impl
  - jjwt-jackson

### 2. 核心工具類 - JwtUtil.java
位置：`src/main/java/com/rex/jwtdemo/util/JwtUtil.java`

功能：
- ✅ 生成 JWT Token (支援自訂 claims)
- ✅ 解析 Token (提取使用者名稱、過期時間、自訂 claims)
- ✅ 驗證 Token 有效性
- ✅ 檢查 Token 是否過期
- ✅ 刷新 Token

使用 HMAC-SHA256 算法進行簽名。

### 3. DTO 類別
- `JwtRequest.java` - 登入請求 DTO
- `JwtResponse.java` - Token 回應 DTO

### 4. REST API Controller - JwtController.java
提供以下端點：
- `POST /api/jwt/login` - 登入並生成 Token
- `POST /api/jwt/validate` - 驗證 Token
- `POST /api/jwt/refresh` - 刷新 Token
- `GET /api/jwt/parse` - 解析 Token 內容

### 5. 配置文件 - application.properties
```properties
jwt.secret=mySecretKeyForJwtTokenGenerationAndValidation12345678
jwt.expiration=3600000  # 1 小時
server.port=8080
```

### 6. 測試文件
- `JwtUtilTest.java` - 單元測試類
- `jwt-test.http` - HTTP 測試檔案（可用 IntelliJ IDEA 直接執行）

### 7. 文檔
- `README.md` - 完整的使用說明文檔

## 如何使用

### 在 IntelliJ IDEA 中

1. **重新載入 Maven 依賴**
   - 右鍵點擊 `pom.xml`
   - 選擇 "Maven" -> "Reload project"
   - 或點擊右上角的 Maven 工具視窗中的刷新按鈕

2. **啟動應用**
   - 運行 `JwtDemoApplication.java` 的 main 方法
   - 或在終端執行：`mvn spring-boot:run`

3. **測試 API**
   - 使用 `jwt-test.http` 檔案測試
   - 在 IntelliJ 中打開該檔案，點擊每個請求旁邊的綠色箭頭執行

### 程式碼範例

```java
@Autowired
private JwtUtil jwtUtil;

// 生成 Token
String token = jwtUtil.generateToken("username");

// 帶自訂資訊
Map<String, Object> claims = new HashMap<>();
claims.put("role", "ADMIN");
String token = jwtUtil.generateToken("username", claims);

// 驗證 Token
boolean isValid = jwtUtil.validateToken(token, "username");

// 提取使用者名稱
String username = jwtUtil.extractUsername(token);

// 刷新 Token
String newToken = jwtUtil.refreshToken(token);
```

## 安全建議

1. **生產環境密鑰管理**
   - 不要將密鑰硬編碼在配置文件中
   - 使用環境變數或配置中心（如 Spring Cloud Config）
   - 定期輪換密鑰

2. **HTTPS**
   - 生產環境必須使用 HTTPS 傳輸 Token

3. **Token 儲存**
   - 前端避免使用 localStorage（容易受 XSS 攻擊）
   - 建議使用 HttpOnly Cookie

4. **過期時間**
   - 根據安全需求調整 Token 有效期
   - 敏感操作使用較短的有效期

5. **刷新機制**
   - 實作 Refresh Token 機制
   - Access Token 短期有效，Refresh Token 長期有效

## 下一步擴展建議

1. **整合 Spring Security**
   - 實作 JWT 過濾器
   - 實作認證管理器
   - 實作授權檢查

2. **Refresh Token 機制**
   - 實作 Refresh Token 存儲（Redis）
   - 實作 Token 黑名單

3. **用戶管理**
   - 整合用戶資料庫
   - 實作用戶註冊、登入、登出

4. **角色權限**
   - 在 Token 中加入角色資訊
   - 實作基於角色的訪問控制（RBAC）

## 故障排除

### Maven 依賴下載失敗
```bash
# 在 IntelliJ IDEA 終端執行
mvn clean install -U
```

### 編譯錯誤
1. 確保 Maven 依賴已正確下載
2. 在 IntelliJ 中：File -> Invalidate Caches / Restart
3. 重新載入 Maven 專案

### Token 驗證失敗
1. 檢查密鑰是否一致
2. 檢查 Token 是否過期
3. 檢查 Token 格式（應為 "Bearer {token}"）

## 專案結構

```
JwtDemo/
├── src/
│   ├── main/
│   │   ├── java/com/rex/jwtdemo/
│   │   │   ├── JwtDemoApplication.java
│   │   │   ├── controller/
│   │   │   │   └── JwtController.java
│   │   │   ├── dto/
│   │   │   │   ├── JwtRequest.java
│   │   │   │   └── JwtResponse.java
│   │   │   └── util/
│   │   │       └── JwtUtil.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/rex/jwtdemo/util/
│           └── JwtUtilTest.java
├── pom.xml
├── README.md
└── jwt-test.http
```

---

完成時間：2025-12-02
版本：1.0

