package org.example.webcrawlerdemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webcrawlerdemo.repository.CrawlArticleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 自由時報 ID 範圍爬蟲服務
 * 根據資料庫最大 ID 或日期推估 ID 範圍，掃描並收集新聞 URL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LtnIdRangeCrawlerService {

    private final CrawlArticleRepository crawlArticleRepository;

    /** 預設起始 ID（當資料庫無資料時使用） */
    private static final int DEFAULT_START_ID = 187390;

    /**
     * 根據資料庫最大 ID 掃描新的新聞 URL
     *
     * @param scanRange   掃描範圍（從最大 ID +1 開始往後掃描 N 個）
     * @param maxArticles 最多找幾筆
     * @return 新聞 URL 列表
     */
    public List<String> scanIdRange(LocalDate targetDate, int scanRange, int maxArticles) {
        List<String> urls = new ArrayList<>();

        // 從資料庫查詢起始 ID
        int startId = getStartIdFromDatabase();
        log.info("起始 ID: {} (來源: {})", startId,
                startId == DEFAULT_START_ID ? "預設值" : "資料庫最大 ID + 1");

        // 從起始 ID 開始掃描
        int count = 0;
        for (int id = startId; id < startId + scanRange && count < maxArticles; id++) {
            String url = buildUrl(id);
            urls.add(url);
            count++;
            log.debug("加入待爬URL: {}", url);
        }

        log.info("共產生 {} 個待爬URL (ID 範圍: {} - {})",
                urls.size(), startId, startId + urls.size() - 1);
        return urls;
    }

    /**
     * 從資料庫查詢起始 ID
     * 邏輯：查詢自由時報最新文章，從 URL 解析 ID，+1 作為起始點
     * 如果沒有資料，使用預設值 187390
     *
     * @return 起始 ID
     */
    private int getStartIdFromDatabase() {
        Optional<Integer> maxId = crawlArticleRepository
                .findFirstBySourceNameOrderBySourceUrlDesc("自由時報")
                .map(article -> extractIdFromUrl(article.getSourceUrl()));

        if (maxId.isPresent()) {
            int nextId = maxId.get() + 1;
            log.info("從資料庫查詢到最大 ID: {}，下次起始 ID: {}", maxId.get(), nextId);
            return nextId;
        } else {
            log.info("資料庫中沒有自由時報資料，使用預設起始 ID: {}", DEFAULT_START_ID);
            return DEFAULT_START_ID;
        }
    }

    /**
     * 從 URL 解析出新聞 ID
     * URL 格式：https://news.ltn.com.tw/news/local/paper/187390
     *
     * @param url 新聞 URL
     * @return 新聞 ID
     */
    private int extractIdFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            log.warn("無法從 URL 解析 ID: {}", url);
            return 0;
        }
    }

    /**
     * 建立自由時報新聞 URL
     * 
     * 格式：https://news.ltn.com.tw/news/local/paper/{ID}
     * 注意：類別可任意，會自動轉向正確類別
     *
     * @param id 新聞 ID
     * @return 完整 URL
     */
    private String buildUrl(int id) {
        return String.format("https://news.ltn.com.tw/news/local/paper/%d", id);
    }
}
