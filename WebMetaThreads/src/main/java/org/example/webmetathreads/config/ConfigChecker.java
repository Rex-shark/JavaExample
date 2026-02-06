package org.example.webmetathreads.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 配置檢查器 - 啟動時驗證環境變數是否正確載入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigChecker implements CommandLineRunner {

    private final ThreadsApiProperties threadsApiProperties;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("🔍 Threads API 配置檢查");
        log.info("========================================");
        log.info("Base URL: {}", threadsApiProperties.getBaseUrl());
        log.info("User ID: {}", threadsApiProperties.getUserId());

        String token = threadsApiProperties.getAccessToken();
        if (token != null && !token.isEmpty()) {
            log.info("Access Token: {}... (長度: {})",
                token.substring(0, Math.min(20, token.length())),
                token.length());
        } else {
            log.warn("⚠️ Access Token 未設定或為空！");
        }

        log.info("========================================");

        // 檢查是否正確載入
        if (threadsApiProperties.getUserId() == null || threadsApiProperties.getUserId().isEmpty()) {
            log.error("❌ THREADS_USER_ID 未載入！請檢查 .env檔案");
        }

        if (threadsApiProperties.getAccessToken() == null || threadsApiProperties.getAccessToken().isEmpty()) {
            log.error("❌ THREADS_ACCESS_TOKEN 未載入！請檢查 .env 檔案");
        }
    }
}
