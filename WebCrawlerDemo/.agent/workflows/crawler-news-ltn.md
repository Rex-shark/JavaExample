---
description: 台灣自由時報(news.ltn.com.tw)功能開發需求說明
---

# 自由時報爬蟲

## 功能說明

模組：**WebCrawlerDemo**

抓取台灣自由時報 (news.ltn.com.tw) 18 年前的新聞資料。

---

## 功能需求

* **日期計算**：依照當日日期，抓 18 年前的新聞資料
  * 範例：今天是 2026/02/05，則去抓 2008/02/05 的新聞資料
* **資料儲存**：抓取新聞資料後，存到 `crawl_article` 資料庫表

---

## 實作方法

### 技術方案：Google Custom Search API + WebMagic

```mermaid
flowchart LR
    A[Google Custom Search API] -->|搜尋 URL| B[URL 列表]
    B -->|WebMagic 爬取| C[新聞內容]
    C -->|DatabasePipeline| D[PostgreSQL]
```

### 核心元件

| 元件 | 檔案 | 說明 |
|------|------|------|
| **Search Service** | `GoogleSearchService.java` | 使用 Custom Search API 搜尋 URL |
| **Page Processor** | `LtnPageProcessor.java` | 解析自由時報新聞頁面 |
| **Crawler Runner** | `LtnCrawlerRunner.java` | 整合搜尋與爬取 |
| **Database Pipeline** | `DatabasePipeline.java` | 儲存到資料庫（共用） |

### 搜尋查詢語法

```
site:news.ltn.com.tw after:2008-02-05 before:2008-02-06
```

---

## 設定檔（application-dev.yml）

```yaml
custom-search:
  api:
    key: AIzaSyDAnV14jMyt9ETmxMbZO-hvQWHmvpdr9g8  # API Key
    cx: 74b9743c7d8574579                          # Search Engine ID
  ltn:
    enabled: false        # 是否啟用（預設關閉）
    test-date: 2008-02-05 # 測試日期
    max-results: 5        # 最大結果數
```

---

## 使用方式

### 啟用爬蟲

修改 `application-dev.yml`：

```yaml
custom-search:
  ltn:
    enabled: true  # 改為 true
```

### 執行

```powershell
.\mvnw.cmd spring-boot:run
```

### 修改日期

```yaml
custom-search:
  ltn:
    test-date: 2008-02-05  # 修改此處
```

---

## API 限制

| 項目 | 限制 |
|------|------|
| 免費配額 | 100 次查詢/天 |
| 單次結果 | 最多 10 筆 |
| 總結果數 | 最多 100 筆 |

---

## 參考文件

* [Google Custom Search API](https://developers.google.com/custom-search/v1/overview)
* [Programmable Search Engine](https://programmablesearchengine.google.com/)
