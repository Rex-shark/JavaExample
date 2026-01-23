# Antigravity Skills 目錄結構設計教學

> 本教學根據 [Google Antigravity Skills 官方教學](https://codelabs.developers.google.com/getting-started-with-antigravity-skills) 撰寫，使用繁體中文說明如何設計 Skills 的目錄結構。

---

## 1. 什麼是 Skill？

Skill 是一種輕量級、可攜帶的任務定義，用於擴展 AI agent 的能力。它包裝了使用工具的方法論，讓 agent 可以自動偵測使用者的意圖，並動態載入所需的專業知識。

### Skill 的特性

| 特性 | 說明 |
|------|------|
| **輕量級** | 使用 Markdown 和 YAML 格式，易於建立和維護 |
| **可攜帶** | 可以在不同專案之間分享和重用 |
| **自動觸發** | Agent 會根據使用者意圖自動選擇合適的 Skill |
| **按需載入** | 只在任務執行時載入，完成後立即釋放 |

---

## 2. Skill 的放置位置

Skills 可以定義在兩個範疇：

### 2.1 工作區範疇（Workspace Scope）

```
<workspace-root>/.agent/skills/
```

- 僅在特定專案內可用
- 適合：專案專用腳本、特定環境部署、資料庫管理、專有框架的樣板程式碼產生

### 2.2 全域範疇（Global Scope）

```
~/.gemini/antigravity/skills/
```

- 在使用者電腦上的所有專案都可使用
- 適合：通用工具（如格式化 JSON、產生 UUID、程式碼風格審查）

---

## 3. 目錄結構設計

### 3.1 基本結構

一個典型的 Skill 目錄結構如下：

```
my-skill/
├── SKILL.md              # 定義檔（必要）
├── scripts/              # 腳本資料夾（選用）
│   ├── run.py
│   └── util.sh
├── resources/            # 資源資料夾（選用）
│   └── template.txt
├── references/           # 參考文件資料夾（選用）
│   └── api-docs.md
└── assets/               # 靜態資源資料夾（選用）
    └── logo.png
```

### 3.2 各資料夾說明

| 資料夾 | 用途 | 必要性 |
|--------|------|--------|
| `SKILL.md` | Skill 的主要定義檔，包含觸發條件和執行指示 | **必要** |
| `scripts/` | 存放 Python、Bash 或 Node 腳本 | 選用 |
| `resources/` | 存放範本、靜態文字等資源 | 選用 |
| `references/` | 存放參考文件或 API 文件 | 選用 |
| `assets/` | 存放圖片、Logo 等靜態資源 | 選用 |

---

## 4. SKILL.md 定義檔

`SKILL.md` 是 Skill 的核心，它告訴 agent：

- 這個 Skill **是什麼**
- **何時**使用它
- **如何**執行它

### 4.1 檔案結構

`SKILL.md` 由兩部分組成：

1. **YAML Frontmatter**（元資料）
2. **Markdown Body**（指示內容）

### 4.2 YAML Frontmatter

這是元資料層，用於讓 agent 索引和匹配 Skill。

```yaml
---
name: my-skill-name
description: 描述這個 Skill 的用途和觸發時機，要足夠具體讓 AI 能正確辨識。
---
```

#### 關鍵欄位

| 欄位 | 必要性 | 說明 |
|------|--------|------|
| `name` | 選用 | 唯一名稱，小寫加連字號（如 `postgres-query`）。若未提供，預設使用資料夾名稱 |
| `description` | **必要** | 最重要的欄位！作為「觸發語句」，必須足夠描述性讓 LLM 辨識語意相關性 |

> ⚠️ **重要提醒**：
>
> - ❌ 模糊的描述：「資料庫工具」
> - ✅ 精確的描述：「對本地 PostgreSQL 資料庫執行唯讀 SQL 查詢，用於檢索使用者或交易資料。適用於除錯資料狀態」

### 4.3 Markdown Body

本體包含執行指示，這是「提示工程」的持久化檔案。當 Skill 被啟用時，此內容會被注入 agent 的上下文視窗。

本體應包含：

1. **目標（Goal）**：清楚說明 Skill 達成的目標
2. **指示（Instructions）**：逐步執行邏輯
3. **範例（Examples）**：輸入/輸出的示範，引導模型表現
4. **限制（Constraints）**：「不要做」的規則

---

## 5. 設計模式等級

根據官方教學，Skills 設計分為 5 個等級：

### Level 1：基本路由（Basic Router）

最簡單的模式，只有 `SKILL.md` 一個檔案。

```
git-commit-formatter/
└── SKILL.md
```

### Level 2：資源運用（Asset Utilization）

將大量靜態文字（如授權條款範本）放在 `resources/` 資料夾，避免浪費 tokens。

```
license-header-adder/
├── SKILL.md
└── resources/
    └── HEADER_TEMPLATE.txt
```

### Level 3：範例學習（Learning by Example）

提供輸入/輸出的黃金範例，讓 LLM 透過模式匹配學習。

```
json-to-pydantic/
├── SKILL.md
└── examples/
    ├── input.json
    └── output.py
```

### Level 4：程序邏輯（Procedural Logic）

使用腳本執行複雜的程序邏輯。

```
database-schema-validator/
├── SKILL.md
└── scripts/
    └── validate_schema.py
```

### Level 5：架構師（The Architect）

結合多個 Skills 和腳本，建立完整的工作流程。

```
adk-tool-scaffold/
├── SKILL.md
├── scripts/
│   └── scaffold.py
├── templates/
│   └── component.template
└── references/
    └── adk-api-docs.md
```

---

## 6. 範例：建立一個完整的 Skill

以下是一個完整的 Skill 範例，使用 Level 2（資源運用）模式：

### 目錄結構

```
license-header-adder/
├── SKILL.md
└── resources/
    └── HEADER_TEMPLATE.txt
```

### SKILL.md 內容

```markdown
---
name: license-header-adder
description: 為新建立的原始碼檔案加上標準的開源授權條款標頭。適用於需要版權聲明的程式碼檔案建立情境。
---

# 授權條款標頭 Skill

這個 Skill 確保所有新建立的原始碼檔案都有正確的版權標頭。

## 指示說明

1. **讀取範本**：首先，讀取位於 `resources/HEADER_TEMPLATE.txt` 的標頭範本檔案內容。
2. **加入檔案開頭**：當建立新檔案時，將範本內容加到目標檔案的最開頭。
3. **調整註解語法**：
   - 對於 C 風格語言（Java、JS、TS、C++），保持 `/* ... */` 區塊格式。
   - 對於 Python、Shell 或 YAML，將區塊轉換為使用 `#` 註解。
   - 對於 HTML/XML，使用 `<!-- ... -->` 格式。

## 注意事項

- 範本內容必須**完全照抄**，不可修改任何文字
- 法律文字不可有任何錯字或遺漏
```

### resources/HEADER_TEMPLATE.txt 內容

```
/*
 * Copyright 2026 [您的公司名稱]
 *
 * 根據 Apache License 2.0 版授權。
 * https://www.apache.org/licenses/LICENSE-2.0
 */
```

---

## 7. 最佳實務

### 7.1 命名規範

- 使用**小寫**加**連字號**（kebab-case）
- 名稱應清楚表達 Skill 的用途
- 範例：`git-commit-formatter`、`license-header-adder`、`json-to-pydantic`

### 7.2 描述撰寫

- 使用具體、動作導向的語言
- 說明**何時**應該使用這個 Skill
- 提供足夠的上下文讓 AI 能正確匹配

### 7.3 關注點分離

遵循軟體工程的標準實務，將不同關注點分開：

| 關注點 | 資料夾 |
|--------|--------|
| 指示（Instruction） | `SKILL.md` |
| 邏輯（Logic） | `scripts/` |
| 知識（Knowledge） | `references/` |
| 資源（Resources） | `resources/` |

---

## 8. 總結

設計 Antigravity Skills 的目錄結構時，請記住：

1. ✅ `SKILL.md` 是唯一必要的檔案
2. ✅ 根據需求選擇適當的設計模式等級
3. ✅ 撰寫精確的 `description` 以確保正確觸發
4. ✅ 將靜態資源放在 `resources/` 以節省 tokens
5. ✅ 使用腳本處理複雜邏輯
6. ✅ 遵循命名規範和最佳實務

---

## 參考資源

- [Google Codelabs: Authoring Antigravity Skills](https://codelabs.developers.google.com/getting-started-with-antigravity-skills)
- [Antigravity Skills GitHub 範例](https://github.com/rominirani/antigravity-skills)
