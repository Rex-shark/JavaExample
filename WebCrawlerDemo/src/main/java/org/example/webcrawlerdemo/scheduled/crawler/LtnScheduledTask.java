package org.example.webcrawlerdemo.scheduled.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webcrawlerdemo.service.crawler.LtnCrawlerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自由時報爬蟲定時任務
 * 根據 cron 表達式自動執行爬蟲
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "crawler.ltn.schedule", name = "enabled", havingValue = "true")
public class LtnScheduledTask {

    private final LtnCrawlerService ltnCrawlerService;

    /**
     * 定時執行爬蟲任務
     * 預設：每天凌晨 2 點執行
     */
    @Scheduled(cron = "${crawler.ltn.schedule.cron:0 0 2 * * ?}")
    public void execute() {
        log.info("⏰ [定時觸發] 自由時報爬蟲排程開始執行");
        ltnCrawlerService.executeCrawl();
    }
}
