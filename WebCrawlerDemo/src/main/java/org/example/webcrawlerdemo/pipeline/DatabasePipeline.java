package org.example.webcrawlerdemo.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webcrawlerdemo.entity.CrawlArticle;
import org.example.webcrawlerdemo.entity.enums.CrawlStatus;
import org.example.webcrawlerdemo.repository.CrawlArticleRepository;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 資料庫儲存 Pipeline
 * 將爬取結果儲存至 PostgreSQL 資料庫
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabasePipeline implements Pipeline {

    private final CrawlArticleRepository crawlArticleRepository;

    /**
     * 處理爬取結果，將資料儲存至資料庫
     *
     * @param resultItems WebMagic 結果物件，包含爬取的資料
     * @param task        WebMagic 任務物件
     */
    @Override
    public void process(ResultItems resultItems, Task task) {
        String url = resultItems.get("url");
        String title = resultItems.get("title");
        String content = resultItems.get("content");
        String author = resultItems.get("author");
        String category = resultItems.get("category");
        String publishTimeStr = resultItems.get("publishTime");

        // 解析發布時間
        LocalDateTime publishedAt = parsePublishTime(publishTimeStr);

        // 檢查是否已存在（組合鍵：URL + 標題 + 發布時間）
        boolean exists;
        if (publishedAt != null) {
            // 有發布時間：使用組合鍵檢查
            exists = crawlArticleRepository.existsBySourceUrlAndTitleAndPublishedAt(url, title, publishedAt);
        } else {
            // 無發布時間：只檢查 URL（降級處理）
            exists = crawlArticleRepository.existsBySourceUrl(url);
        }

        if (exists) {
            log.info("文章已存在，跳過儲存: {}", title);
            return;
        }

        // 根據 URL 判斷來源
        String sourceName = determineSourceName(url);

        // 建立 CrawlArticle 實體
        CrawlArticle article = CrawlArticle.builder()
                .sourceUrl(url)
                .sourceName(sourceName)
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .publishedAt(publishedAt)
                .status(CrawlStatus.PENDING)
                .build();

        try {
            crawlArticleRepository.save(article);
            log.info("✅ 文章已儲存至資料庫: {} (ID: {})", title, article.getId());
        } catch (Exception e) {
            log.error("❌ 儲存文章失敗: {}", title, e);
        }
    }

    /**
     * 解析發布時間字串
     * 支援多種格式：
     * 1. 自由時報格式: 2008/02/05 06:00
     * 2. ISO 8601 格式: 2026-02-04T10:00:00Z
     *
     * @param publishTimeStr 發布時間字串
     * @return LocalDateTime 或 null
     */
    private LocalDateTime parsePublishTime(String publishTimeStr) {
        if (publishTimeStr == null || publishTimeStr.isBlank()) {
            return null;
        }

        // 格式 1: 自由時報格式 (2008/02/05 06:00)
        try {
            DateTimeFormatter ltnFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            return LocalDateTime.parse(publishTimeStr.trim(), ltnFormatter);
        } catch (DateTimeParseException e) {
            // 繼續嘗試其他格式
        }

        // 格式 2: ISO 8601 格式 (2026-02-04T10:00:00Z)
        try {
            return LocalDateTime.parse(publishTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            // 繼續嘗試其他格式
        }

        log.warn("無法解析發布時間: {}", publishTimeStr);
        return null;
    }

    /**
     * 根據 URL 判斷來源名稱
     *
     * @param url 來源 URL
     * @return 來源名稱
     */
    private String determineSourceName(String url) {
        if (url == null) {
            return "未知來源";
        }

        if (url.contains("techcrunch.com")) {
            return "TechCrunch";
        } else if (url.contains("news.ltn.com.tw")) {
            return "自由時報";
        } else {
            return "未知來源";
        }
    }
}
