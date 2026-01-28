## 後端開發準則 (Backend Guidelines)

* 語言與框架
  * 使用 Java 21 (LTS)
  * 使用 Spring Boot 3.4.x (最新穩定版)
  * 專案管理工具：Maven
  * Lombok：使用 @Data, @Builder, @Slf4j 等註解簡化程式碼


* API 設計與規範 
  * 撰寫 OpenAPI (Swagger) 規格書，並自動生成介面與 DTO
  * 風格：RESTful API
  * 文件：使用 Swagger UI / OpenAPI 3.0

* 資料庫與持續性
  * 資料庫：MySQL 8.0+
  * ORM 框架：Spring Data JPA

* 安全性
  * 框架：Spring Security 6
  * 機制：使用 JWT (JSON Web Tokens) 進行無狀態 (Stateless) 身份驗證

* 測試與品質
  * 單元測試：JUnit 5 + Mockito
  * 日誌：使用 @Slf4j (配合 Logback)

* 架構與設計
  * 遵循 SOLID 原則
  * 使用物件導向設計 (OOD)
  * 遵循好的設計模式(design pattern)
  * 物件轉換：使用 MapStruct 進行 DTO 與 Entity 轉換

* 異常處理：使用 @RestControllerAdvice 統一處理例外，回傳標準 Error Response


#  前端開發準則 (Frontend Guidelines)

* 核心技術
  * 框架：Vue 3 (Composition API, <script setup>)
  * 語言：JavaScript 
  * 建構工具：Vite

* 架構與路由
  * 路由管理：Vue Router
  * 狀態管理：Pinia 

#  規範

* 註解：繁體中文，台灣用語
* 命名規範：採用 camelCase 命名法
* 檔案結構：模組化，依功能區分資料
* 程式碼風格：ESLint + Prettier ，遵循官方推薦規範


# 開發流程

1. 閱讀本規範並確認理解
2. 閱讀spec/specify/structure.md 了解專案結構
3. 根據任務需求(spec/specify/specify.md)，撰寫程式碼並遵循本規範