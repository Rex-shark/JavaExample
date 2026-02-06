package org.example.webcrawlerdemo.controller.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webcrawlerdemo.service.crawler.TechCrunchCrawlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TechCrunch 爬蟲控制器
 * 提供手動觸發爬蟲的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler/techcrunch")
@RequiredArgsConstructor
public class TechCrunchCrawlerController {

    private final TechCrunchCrawlerService techCrunchCrawlerService;

    /**
     * 手動執行爬蟲
     *
     * @return 執行結果
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> manualExecute() {
        log.info("🖱️ [手動觸發] 收到 API 請求，準備執行 TechCrunch 爬蟲");

        Map<String, Object> response = new HashMap<>();

        try {
            techCrunchCrawlerService.executeCrawl();
            response.put("success", true);
            response.put("message", "爬蟲已觸發執行");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("手動觸發爬蟲失敗", e);
            response.put("success", false);
            response.put("message", "爬蟲執行失敗：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 查詢爬蟲狀態
     *
     * @return 爬蟲狀態
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        boolean isRunning = techCrunchCrawlerService.isRunning();

        response.put("source", "TechCrunch");
        response.put("isRunning", isRunning);
        response.put("status", isRunning ? "執行中" : "空閒");

        return ResponseEntity.ok(response);
    }
}
