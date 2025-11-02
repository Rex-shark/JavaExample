# game-docker1 — 使用者 CRUD 教學前端

這是以 HTML5 Boilerplate 為基底的前端，首頁改為簡單的「使用者 CRUD 教學」頁面。

後端 API 由模組 DockerDemo 提供，端點為 http://localhost:8080/users。

允許的 CORS 來源包含 http://localhost:5173 與 http://localhost:3000。本專案預設於 5173 埠啟動開發伺服器。

## 快速開始（Windows/cmd.exe）

1) 安裝依賴

- 於 `game-docker1` 目錄安裝依賴。

2) 啟動後端（請在 DockerDemo 模組啟動 Spring Boot，必須監聽 8080 埠）

3) 啟動前端開發伺服器（5173 埠）

- 啟動後瀏覽 http://localhost:5173/ 即可看到 CRUD 頁面。

- 新增：POST http://localhost:8080/users
- 查詢列表：GET http://localhost:8080/users
- 更新：PUT http://localhost:8080/users/{uuid}
- 刪除：DELETE http://localhost:8080/users/{uuid}

注意：
- 新增時需提供 account(>=6 字元), password, createdUserId；uuid 可空白（後端自動產生）。
- 更新時可選擇輸入 password（留空代表不變），updateUserId 會一併傳入。

## 產品環境建置

- 產出將在 `dist/` 目錄，可由任意靜態伺服器提供。
