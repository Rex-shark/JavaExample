# SpecDemo 專案結構

## 技術棧

| 分類 | 技術 |
|------|------|
| 語言 | Java 17 |
| 框架 | Spring Boot 3.5.9 |
| ORM | Spring Data JPA |
| 安全 | Spring Security 6 + JWT |
| 資料庫 | MySQL 8.0+ |
| API 文件 | SpringDoc OpenAPI (Swagger) |
| 建構工具 | Maven |

---

## 目錄結構

```
src/main/java/com/rex/specdemo/
├── SpecDemoApplication.java          # 應用程式入口
├── config/
│   ├── JwtAuthenticationFilter.java  # JWT 認證過濾器
│   ├── OpenApiConfig.java            # Swagger 配置
│   └── SecurityConfig.java           # Spring Security 配置
├── controller/
│   └── AuthController.java           # 認證 API（註冊/登入）
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java         # 登入請求
│   │   └── RegisterRequest.java      # 註冊請求
│   └── response/
│       ├── ApiResponse.java          # 統一回應格式
│       ├── LoginResponse.java        # 登入回應
│       └── MemberResponse.java       # 會員資訊回應
├── entity/
│   ├── Member.java                   # 會員實體
│   ├── Menu.java                     # 系統菜單實體
│   ├── Permission.java               # 權限實體
│   └── Role.java                     # 角色實體
├── exception/
│   ├── BusinessException.java        # 業務邏輯例外
│   └── GlobalExceptionHandler.java   # 全域例外處理
├── repository/
│   ├── MemberRepository.java
│   ├── MenuRepository.java
│   ├── PermissionRepository.java
│   └── RoleRepository.java
└── service/
    ├── JwtService.java               # JWT 服務
    ├── MemberService.java            # 會員服務介面
    └── impl/
        └── MemberServiceImpl.java    # 會員服務實作
```

---

## RBAC 權限架構

```
Member（會員）
   │
   └──┬── MemberRole（關聯表）
      │
      ▼
   Role（角色）
      │
      ├──┬── RolePermission（關聯表）
      │  │
      │  ▼
      │  Permission（權限）
      │
      └──┬── RoleMenu（關聯表）
         │
         ▼
         Menu（系統菜單）
```

---

## API 端點

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/auth/register` | 會員註冊 |
| POST | `/api/auth/login` | 會員登入 |

---

## 資料庫資料表

| 表名 | 說明 |
|------|------|
| member | 會員 |
| role | 角色 |
| permission | 權限 |
| menu | 系統菜單 |
| member_role | 會員-角色關聯 |
| role_permission | 角色-權限關聯 |
| role_menu | 角色-菜單關聯 |
