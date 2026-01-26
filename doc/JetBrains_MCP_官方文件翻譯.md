# Model Context Protocol (MCP) - JetBrains 官方文件翻譯

> 原文來源：JetBrains IDE (IntelliJ IDEA) 官方 MCP 文件

---

## 模型情境協定 (MCP)

### 新增 MCP 伺服器

要將 Junie（或 Copilot Agent）連接到 MCP 伺服器，請使用 Junie 設定中的 `mcp.json` 檔案。

---

## 使用者授權

您可以授權 Junie 執行 MCP 命令而無需每次都核准：

1. **透過 Action Allowlist 設定頁面**
   - 在設定中找到「Action Allowlist」（動作允許清單）頁面
   - 將 MCP 工具加入到允許清單中

2. **透過 Junie 工具視窗**
   - 當 Junie 執行命令時，點擊命令旁邊的 ✓ 按鈕
   - 直接將該動作加入允許清單

**將 MCP 工具加入 Action Allowlist**

---

## 專案層級設定

要在專案層級設定 MCP 伺服器：

1. 在專案根目錄建立 `.junie/mcp/` 資料夾
2. 手動在該資料夾中加入 `mcp.json` 檔案

這樣可以讓每個專案有自己獨立的 MCP 設定。

---

## MCP 設定

### 新增 MCP 伺服器到 Junie

**MCP 伺服器配置檔案位置：**

- **全域設定：** `AppData\Local\github-copilot\intellij\mcp.json`
- **專案設定：** `<專案根目錄>/.junie/mcp/mcp.json`

### 支援的工具

MCP 伺服器提供各種工具，執行結果會回傳以下狀態：

| 狀態 | 說明 |
|------|------|
| ✅ **ok** | 操作成功完成 |
| ❌ **project dir not found** | 無法確定專案目錄 |
| ❌ **file not found** | 指定的檔案不存在 |
| ❌ **could not get document** | 無法存取檔案內容 |

---

## 外部客戶端設定

### 自動設定

對於以下外部客戶端，可以執行**自動設定**：

- **Claude Code**
- **Claude Desktop**
- **Cursor**
- **VS Code**
- **Codex**
- **Windsurf**

這些客戶端支援自動偵測並連接到 JetBrains IDE 的 MCP 伺服器。

### 手動設定

如果您想從**其他客戶端**連接到 MCP 伺服器，需要執行**手動設定**。

---

## 無需確認即可執行動作

MCP 伺服器允許已連接的外部客戶端在 IDE 中執行終端命令或執行設定，而無需每次都提示使用者確認。

### 啟用此模式：

1. **前往設定**
   - `Settings` → `Tools` → `MCP Server`

2. **啟用無確認模式**
   - 勾選「Execute actions without confirmation」（無需確認即可執行動作）

3. **設定允許清單**
   - 指定哪些命令或工具可以自動執行
   - 建議只對信任的伺服器啟用此功能

---

## mcp.json 設定檔格式

### 基本結構

```json
{
    "servers": {
        "server-name": {
            "type": "stdio",
            "command": "command-to-run",
            "args": [],
            "env": {
                "TOKEN": "your_token"
            }
        }
    }
}
```

### 支援的伺服器類型

#### 1. 本地伺服器 (stdio)

使用標準輸入/輸出通訊：

```json
{
    "servers": {
        "my-mcp-server": {
            "type": "stdio",
            "command": "node",
            "args": ["path/to/server.js"],
            "env": {
                "API_KEY": "your_api_key"
            }
        }
    }
}
```

#### 2. 遠端伺服器 (SSE/Streamable HTTP)

透過 HTTP 連接：

```json
{
    "servers": {
        "github": {
            "url": "https://api.githubcopilot.com/mcp/",
            "requestInit": {
                "headers": {
                    "Authorization": "Bearer your_token"
                }
            }
        }
    }
}
```

---

## 實際設定範例

### 範例 1：GitHub MCP Server (遠端)

```json
{
    "servers": {
        "github": {
            "url": "https://api.githubcopilot.com/mcp/",
            "requestInit": {
                "headers": {
                    "Authorization": "Bearer ghp_your_github_token"
                }
            }
        }
    }
}
```

### 範例 2：本地 Node.js MCP Server

```json
{
    "servers": {
        "my-local-server": {
            "type": "stdio",
            "command": "node",
            "args": ["C:\\path\\to\\server.js"],
            "env": {
                "PORT": "3000",
                "API_KEY": "secret_key"
            }
        }
    }
}
```

### 範例 3：使用 npx 執行的伺服器

```json
{
    "servers": {
        "postgres": {
            "type": "stdio",
            "command": "npx",
            "args": ["-y", "@modelcontextprotocol/server-postgres"],
            "env": {
                "DATABASE_URL": "postgresql://user:pass@localhost:5432/db"
            }
        }
    }
}
```

### 範例 4：Java MCP Server (自訂)

```json
{
    "servers": {
        "my-java-server": {
            "type": "stdio",
            "command": "java",
            "args": [
                "-jar",
                "C:\\Users\\rexre\\IdeaProjects\\JavaExample\\McpServerDemo\\target\\McpServerDemo-0.0.1-SNAPSHOT.jar"
            ],
            "env": {
                "SPRING_PROFILES_ACTIVE": "prod"
            }
        }
    }
}
```

---

## 設定檔案位置

### Windows

**全域設定：**
```
C:\Users\<使用者名稱>\AppData\Local\github-copilot\intellij\mcp.json
```

**專案設定：**
```
<專案根目錄>\.junie\mcp\mcp.json
```

### macOS / Linux

**全域設定：**
```
~/.config/github-copilot/intellij/mcp.json
```

**專案設定：**
```
<專案根目錄>/.junie/mcp/mcp.json
```

---

## 常見問題

### Q1: 全域設定和專案設定的差異？

- **全域設定**：套用到所有 IntelliJ IDEA 專案
- **專案設定**：只套用到特定專案，優先權高於全域設定

### Q2: 如何檢查 MCP 伺服器是否成功連接？

1. 開啟 Copilot Chat / Junie 面板
2. 查看工具列表，應該會顯示已連接的 MCP 工具
3. 檢查 IDE 的 Event Log，查看連接狀態

### Q3: 為什麼我的 MCP 伺服器無法啟動？

常見原因：
- ✓ 檢查 `command` 路徑是否正確
- ✓ 確認環境變數設定正確
- ✓ 查看 IDE 的 Event Log 錯誤訊息
- ✓ 確認伺服器程式可以獨立執行

### Q4: 可以同時設定多個 MCP 伺服器嗎？

可以！在 `servers` 物件中加入多個伺服器設定：

```json
{
    "servers": {
        "github": { ... },
        "postgres": { ... },
        "my-custom-server": { ... }
    }
}
```

---

## 安全性建議

### ⚠️ 重要提醒

1. **不要將包含敏感資訊的 mcp.json 提交到版本控制**
   - 使用 `.gitignore` 排除此檔案
   - 或使用環境變數替代直接寫入 Token

2. **定期更新 API Token**
   - 設定 Token 過期時間
   - 使用最小權限原則

3. **謹慎使用「無需確認執行」功能**
   - 只對信任的伺服器啟用
   - 定期檢查 Action Allowlist

4. **使用環境變數儲存敏感資訊**

```json
{
    "servers": {
        "github": {
            "type": "stdio",
            "command": "node",
            "args": ["server.js"],
            "env": {
                "GITHUB_TOKEN": "${env:GITHUB_TOKEN}"
            }
        }
    }
}
```

---

## 相關資源

### 官方文件

- [GitHub Copilot MCP 文件](https://docs.github.com/en/copilot/customizing-copilot/extending-copilot-chat-with-mcp?tool=jetbrains)
- [Model Context Protocol 規範](https://modelcontextprotocol.io/)
- [JetBrains Junie 文件](https://www.jetbrains.com/help/idea/junie.html)

### 範例專案

- 本專案中的 `McpServerDemo` - Java 實作的 MCP Server
- [@modelcontextprotocol/servers](https://github.com/modelcontextprotocol/servers) - 官方範例伺服器集合

---

## 下一步

1. ✅ 建立或編輯 `mcp.json` 設定檔
2. 🔧 設定您需要的 MCP 伺服器（GitHub、Database 等）
3. 🚀 在 Copilot Chat 中測試 MCP 功能
4. 📝 建立專案專屬的 MCP 設定（選用）
5. 🛠️ 開發自訂 MCP Server（進階）

---

**文件翻譯日期：2026-01-24**  
**原文來源：JetBrains IntelliJ IDEA MCP Documentation**
