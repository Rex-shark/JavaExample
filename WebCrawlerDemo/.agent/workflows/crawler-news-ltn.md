---
description: 自由時報新聞爬蟲使用指南（排程 + 手動觸發）
---

# 自由時報新聞爬蟲

## 功能說明

自動從自由時報（news.ltn.com.tw）抓取新聞文章，支援定時執行和手動觸發。

**爬取策略：** 基於資料庫最大 ID 延續，每次從上次停止的地方繼續抓取。

---

## 快速開始

### 1. 啟用定時執行

編輯 `application-dev.yml`：

```yaml
crawler:
  ltn:
    schedule:
      enabled: true              # 啟用排程
      cron: "0 0 2 * * ?"        # 每天凌晨 2 點
    scan-range: 100
    max-articles: 100
```

### 2. 手動觸發

```bash
curl -X POST http://localhost:8082/api/crawler/ltn/execute
```

### 3. 查詢狀態

```bash
curl http://localhost:8082/api/crawler/ltn/status
```

---

## 配置說明

| 參數 | 說明 | 預設值 |
|------|------|--------|
| `schedule.enabled` | 是否啟用定時執行 | `false` |
| `schedule.cron` | 排程時間（Cron 表達式） | `0 0 2 * * ?`（02:00） |
| `scan-range` | 每次掃描的 ID 範圍 | `100` |
| `max-articles` | 最多抓取幾篇 | `100` |

---

## Cron 時間範例

| 時間 | Cron 表達式 |
|------|------------|
| 每天凌晨 2 點 | `0 0 2 * * ?` |
| 每天晚上 9、10、11 點 | `0 0 21,22,23 * * ?` |
| 每小時 | `0 0 * * * ?` |
| 每 30 分鐘 | `0 */30 * * * ?` |

---

## 技術架構

### 核心組件

1. **LtnCrawlerService** - 爬蟲核心邏輯（含併發鎖）
2. **LtnIdRangeCrawlerService** - ID 範圍計算與 URL 生成
3. **LtnScheduledTask** - 定時任務（@Scheduled）
4. **LtnCrawlerController** - REST API（手動觸發）

### 資料庫 ID 延續機制

```
第 1 次執行：
  → 資料庫無資料
  → 使用預設 ID: 187390
  → 抓取 187390 - 187489

第 2 次執行：
  → 查詢資料庫最大 ID: 187489
  → 下次起始 ID: 187490
  → 抓取 187490 - 187589

持續累積...
```

### 併發控制

使用 `AtomicBoolean` 鎖，防止同時觸發多次：

```
定時觸發 02:00 → 執行中 ✅
手動觸發 02:01 → 被拒絕 ❌（防止重複）
```

---

## REST API

### 執行爬蟲

**請求：**

```bash
POST http://localhost:8082/api/crawler/ltn/execute
```

**回應：**

```json
{
  "success": true,
  "message": "爬蟲已觸發執行"
}
```

### 查詢狀態

**請求：**

```bash
GET http://localhost:8082/api/crawler/ltn/status
```

**回應：**

```json
{
  "source": "自由時報",
  "isRunning": false,
  "status": "空閒"
}
```

---

## 日誌範例

```
⏰ [定時觸發] 自由時報爬蟲排程開始執行
╔══════════════════════════════════════╗
║     自由時報爬蟲開始執行              ║
╠══════════════════════════════════════╣
║ 掃描範圍: 100 個 ID
║ 執行緒: scheduled-task-1
╚══════════════════════════════════════╝
起始 ID: 187490 (來源: 資料庫最大 ID + 1)
共產生 100 個待爬URL (ID 範圍: 187490 - 187589)
✅ 自由時報爬蟲執行完成！
```

---

## 注意事項

1. **第一次執行**：從預設 ID `187390` 開始（2008/02/05 的新聞）
2. **防重複機制**：資料庫已存在的文章會自動跳過
3. **併發保護**：同時觸發會被拒絕，確保只有一個在執行
4. **ID 間隔**：URL 可能不連續（404 會跳過）

---

## 疑難排解

**問題 1：爬蟲沒有自動執行**

- 檢查 `schedule.enabled` 是否為 `true`
- 檢查 `cron` 表達式是否正確
- 查看日誌確認排程是否啟動

**問題 2：手動觸發失敗**

- 確認應用程式正在運行
- 檢查 port 8082 是否正確
- 查看日誌錯誤訊息

**問題 3：重複抓取相同文章**

- 檢查資料庫連線是否正常
- 確認防重複機制是否生效

---

## 相關檔案

- 配置：`application-dev.yml`
- Service：`service/crawler/LtnCrawlerService.java`
- ID 計算：`service/crawler/LtnIdRangeCrawlerService.java`
- 排程：`scheduled/crawler/LtnScheduledTask.java`
- API：`controller/crawler/LtnCrawlerController.java`
