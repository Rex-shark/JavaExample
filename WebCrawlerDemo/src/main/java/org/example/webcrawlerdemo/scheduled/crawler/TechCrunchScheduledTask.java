package org.example.webcrawlerdemo.scheduled.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webcrawlerdemo.service.crawler.TechCrunchCrawlerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * TechCrunch 爬蟲定時任務
 * 根據 cron 表達式自動執行爬蟲
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "crawler.techcrunch.schedule", name = "enabled", havingValue = "true")
public class TechCrunchScheduledTask {

    private final TechCrunchCrawlerService techCrunchCrawlerService;

    /**
     * 定時執行爬蟲任務
     * 預設：每天凌晨 3 點執行
     */
    @Scheduled(cron = "${crawler.techcrunch.schedule.cron:0 0 3 * * ?}")
    public void execute() {
        log.info("⏰ [定時觸發] TechCrunch 爬蟲排程開始執行");
        techCrunchCrawlerService.executeCrawl();
    }
}
