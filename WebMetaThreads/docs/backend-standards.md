---
description: 後端架構與命名規範指引
---

# 後端架構與命名規範

本專案後端使用 Spring Boot 3 + Java 21 + Maven 進行開發。

---

## 註解規範

- **所有類別、方法、欄位皆須加上註解**
- **註解語言：繁體中文**
- 使用 Javadoc 格式 (`/** */`)
- Swagger 註解使用繁體中文描述
- **所有 Service/Controller 必須使用 `@Slf4j`**

---

## API 回應規範

所有 API 必須使用統一回應格式 `ApiResponse<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2026-01-29T09:00:00",
  "traceId": "abc123def456"
}
```

### Controller 範例

```java
@Slf4j
@RestController
@RequestMapping("/api/xxx")
public class XxxController {

    @GetMapping
    public ApiResponse<List<XxxResponse>> getAll() {
        return ApiResponse.success(service.getAll());
    }

    @PostMapping
    public ApiResponse<XxxResponse> create(@Valid @RequestBody XxxRequest request) {
        log.info("建立 Xxx: {}", request.name());
        return ApiResponse.success("建立成功", service.create(request));
    }
}
```

---

## DTO 命名規範

| 類型 | 命名格式 | 範例 | 說明 |
|------|---------|-----|------|
| **請求 DTO** | `{Entity}Request` | `UserCreateRequest`, `UserUpdateRequest` | 接收客戶端請求 |
| **回應 DTO** | `{Entity}Response` | `UserResponse`, `UserDetailResponse` | 回傳給客戶端 |
| **查詢 DTO** | `{Entity}Query` | `UserSearchQuery` | 查詢條件封裝 |

---

## 例外處理規範

### 1. 使用 BusinessException 拋出業務錯誤

```java
if (repository.existsByCode(code)) {
    throw new BusinessException(ResponseCode.XXX_ALREADY_EXISTS);
}
```

### 2. ResponseCode 枚舉

| 類型 | 範圍 | 說明 |
|------|------|------|
| 成功 | 200 | SUCCESS |
| 客戶端錯誤 | 4xx | BAD_REQUEST, UNAUTHORIZED, FORBIDDEN |
| 伺服器錯誤 | 5xx | INTERNAL_ERROR |
| 業務錯誤 | 6xx | 自定義業務錯誤碼 |

### 3. GlobalExceptionHandler 統一處理

所有異常由 `@RestControllerAdvice` 統一攔截並轉換為 `ApiResponse` 格式。

---

## 驗證規範

### 1. 使用 Jakarta Validation

```java
public record UserRequest(
    @NotBlank(message = "使用者名稱不能為空")
    @Size(min = 3, max = 20, message = "使用者名稱長度必須在 3-20 之間")
    String username,
    
    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式不正確")
    String email
) {}
```

### 2. 自訂驗證器

對於複雜的業務邏輯驗證，使用自訂 Validator。

---

## 常數枚舉規範

常用常數必須使用 Enum，禁止使用魔法數字或字串。

| Enum | 用途 |
|------|------|
| `ResponseCode` | API 回應狀態碼 |
| `PermissionType` | 權限類型 (DIR, MENU, BTN) |

---

## 分頁規範

使用 Spring Data 的 `Page` 和 `Pageable`：

```java
@GetMapping
public ApiResponse<Page<UserResponse>> getUsers(
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
) {
    return ApiResponse.success(userService.findAll(pageable));
}
```

---

## 日誌規範

### 1. 使用 @Slf4j

```java
@Slf4j
@Service
public class XxxService {
    public void method() {
        log.info("執行操作: {}", param);
        log.debug("詳細資訊: {}", detail);
        log.error("發生錯誤: {}", ex.getMessage(), ex);
    }
}
```

### 2. 日誌等級

| 等級 | 用途 |
|------|------|
| `ERROR` | 系統錯誤、異常 |
| `WARN` | 業務警告、非預期狀況 |
| `INFO` | 重要業務操作 (建立/更新/刪除) |
| `DEBUG` | 詳細除錯資訊 |

### 3. TraceId 追蹤

每個請求自動產生 `traceId`，可在日誌中追蹤完整請求鏈。

---

## Repository 命名規範

| 方法類型 | 命名格式 | 範例 |
|---------|----------|------|
| 查詢 | `findBy{Condition}` | `findByUsername`, `findByEmailAndDeletedAtIsNull` |
| 檢查存在 | `existsBy{Condition}` | `existsByUsername` |
| 刪除 | `deleteBy{Condition}` | `deleteById` |
| 統計 | `countBy{Condition}` | `countByStatus` |

---

## SQL 命名規範

| 類型 | 規範 | 範例 |
|------|------|------|
| **資料庫欄位** | `snake_case` | `created_at`, `user_id` |
| **Java 屬性** | `camelCase` | `createdAt`, `userId` |
| **表名** | `snake_case` + 前綴 | `sys_user`, `sys_role` |
| **主鍵** | `id` | `id` |
| **外鍵** | `{table}_id` | `user_id`, `role_id` |
| **索引** | `idx_{table}_{column}` | `idx_user_username` |

---

## Entity 繼承結構

```text
BaseEntity                 # 基礎 Entity（真刪除）
├── id
├── createdAt
└── updatedAt

SoftDeleteEntity           # 軟刪除 Entity
├── extends BaseEntity
└── deletedAt
```

---

## 軟刪除策略

| 表 | 軟刪除 | 說明 |
|---|--------|------|
| `sys_user` | ✅ 是 | 保留用戶歷史 |
| `sys_group` | ✅ 是 | 保留組織歷史 |
| `sys_role` | ✅ 是 | 避免關聯錯誤 |
| `sys_permission` | ✅ 是 | 避免關聯錯誤 |
| 關聯表 | ❌ 否 | 物理刪除 |

---

## 安全性規範

### 1. 密碼處理

- 使用 BCrypt 加密
- 禁止明文儲存或記錄密碼

### 2. 敏感資料

- API Response 中不得包含敏感資訊 (密碼、Token 等)
- 使用 `@JsonIgnore` 標註敏感欄位

### 3. SQL 注入防護

- 使用 JPA/Hibernate 參數化查詢
- 禁止字串拼接 SQL

---

## 測試規範

### 1. 測試分類

- Unit Test: `XxxServiceTest`
- Integration Test: `XxxControllerTest`
- Repository Test: `XxxRepositoryTest`

### 2. 測試覆蓋率

- Service 層: 80% 以上
- Controller 層: 70% 以上

---

## Maven Wrapper (mvnw) 使用規範

> [!IMPORTANT]
> **本專案必須使用 `mvnw` 指令，禁止直接使用 `mvn` 指令。**

### 常用指令 (Windows)

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
.\mvnw.cmd compile -q
.\mvnw.cmd test
```

### 常用指令對照表

| 功能 | ❌ 錯誤 | ✅ 正確 (Windows) |
|------|---------|------------------|
| 編譯 | `mvn compile` | `.\mvnw.cmd compile` |
| 執行專案 | `mvn spring-boot:run` | `.\mvnw.cmd spring-boot:run` |
| 執行測試 | `mvn test` | `.\mvnw.cmd test` |
| 打包 | `mvn package` | `.\mvnw.cmd package` |

---

## 環境配置

| 檔案 | 用途 |
|------|------|
| `application.yml` | 共用設定 |
| `application-dev.yml` | 開發環境 |
| `application-prod.yml` | 生產環境 (使用環境變數) |
| `.env` | 環境變數配置 (不納入版控) |

**注意**: `.env` 檔案包含敏感資訊，必須加入 `.gitignore`。

## 參考標準

- Google Cloud SQL Style Guide
- Netflix Data Team Standards
- Stripe API Design Guidelines
