package org.example.webcrawlerdemo.service.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webcrawlerdemo.pipeline.DatabasePipeline;
import org.example.webcrawlerdemo.processor.TechCrunchPageProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TechCrunch 爬蟲服務
 * 提供爬蟲核心邏輯，包含併發控制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TechCrunchCrawlerService {

    private final DatabasePipeline databasePipeline;

    /** 併發鎖：防止同一個爬蟲被重複觸發 */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Value("${crawler.techcrunch.max-articles:50}")
    private int maxArticles;

    @Value("${crawler.thread-count:1}")
    private int threadCount;

    /**
     * 執行爬蟲任務
     * 包含併發控制，防止重複執行
     */
    public void executeCrawl() {
        // 嘗試獲取鎖
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("⚠️ TechCrunch 爬蟲正在執行中，跳過本次觸發");
            return;
        }

        try {
            log.info("╔══════════════════════════════════════════════════════════════╗");
            log.info("║           TechCrunch 爬蟲開始執行                            ║");
            log.info("╠══════════════════════════════════════════════════════════════╣");
            log.info("║ 最多抓取: {} 篇", maxArticles);
            log.info("║ 執行緒: {}", Thread.currentThread().getName());
            log.info("╚══════════════════════════════════════════════════════════════╝");

            // 執行實際爬蟲邏輯
            doActualCrawl();

            log.info("✅ TechCrunch 爬蟲執行完成！");
        } catch (Exception e) {
            log.error("❌ TechCrunch 爬蟲執行失敗", e);
            throw new RuntimeException("爬蟲執行失敗", e);
        } finally {
            // 釋放鎖
            isRunning.set(false);
        }
    }

    /**
     * 實際爬蟲邏輯
     */
    private void doActualCrawl() {
        log.info("開始爬取 TechCrunch 最新文章...");

        // TechCrunch 首頁 URL
        String startUrl = "https://techcrunch.com/";

        // 建立爬蟲
        Spider spider = Spider.create(new TechCrunchPageProcessor())
                .addUrl(startUrl)
                .addPipeline(databasePipeline)
                .thread(threadCount);

        // 執行爬取
        spider.run();
    }

    /**
     * 檢查爬蟲是否正在執行
     *
     * @return true 如果正在執行
     */
    public boolean isRunning() {
        return isRunning.get();
    }
}
