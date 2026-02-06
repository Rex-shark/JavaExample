package org.example.webcrawlerdemo.service.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webcrawlerdemo.pipeline.DatabasePipeline;
import org.example.webcrawlerdemo.processor.LtnPageProcessor;
import org.example.webcrawlerdemo.service.LtnIdRangeCrawlerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自由時報爬蟲服務
 * 提供爬蟲核心邏輯，包含併發控制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LtnCrawlerService {

    private final LtnIdRangeCrawlerService ltnIdRangeCrawlerService;
    private final DatabasePipeline databasePipeline;

    /** 併發鎖：防止同一個爬蟲被重複觸發 */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Value("${crawler.ltn.scan-range:100}")
    private int scanRange;

    @Value("${crawler.ltn.max-articles:100}")
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
            log.warn("⚠️ 自由時報爬蟲正在執行中，跳過本次觸發");
            return;
        }

        try {
            log.info("╔══════════════════════════════════════════════════════════════╗");
            log.info("║           自由時報爬蟲開始執行                                ║");
            log.info("╠══════════════════════════════════════════════════════════════╣");
            log.info("║ 掃描範圍: {} 個 ID", scanRange);
            log.info("║ 最多抓取: {} 筆", maxArticles);
            log.info("║ 執行緒: {}", Thread.currentThread().getName());
            log.info("╚══════════════════════════════════════════════════════════════╝");

            // 執行實際爬蟲邏輯
            doActualCrawl();

            log.info("✅ 自由時報爬蟲執行完成！");
        } catch (Exception e) {
            log.error("❌ 自由時報爬蟲執行失敗", e);
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
        // 步驟 1: 掃描 ID 範圍，收集 URL
        log.info("步驟 1: 掃描 ID 範圍，收集待爬 URL...");
        List<String> urls = ltnIdRangeCrawlerService.scanIdRange(
                null, // targetDate 參數不再使用
                scanRange,
                maxArticles);

        if (urls.isEmpty()) {
            log.warn("未找到任何 URL");
            return;
        }

        log.info("找到 {} 個待爬 URL", urls.size());

        // 步驟 2: 使用 WebMagic 爬取這些 URL 的內容
        log.info("步驟 2: 開始爬取新聞內容...");
        Spider spider = Spider.create(new LtnPageProcessor())
                .addPipeline(databasePipeline)
                .thread(threadCount);

        // 將所有 URL 加入爬取佇列
        for (String url : urls) {
            spider.addUrl(url);
        }

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
