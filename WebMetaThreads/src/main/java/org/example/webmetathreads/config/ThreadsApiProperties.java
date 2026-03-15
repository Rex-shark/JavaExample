package org.example.webmetathreads.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Threads API 設定屬性
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "threads.api")
public class ThreadsApiProperties {

    /**
     * API 基礎 URL
     */
    private String baseUrl = "https://graph.threads.net/v1.0";

    /**
     * Threads 用戶 ID
     */
    private String userId;

    /**
     * Access Token
     */
    private String accessToken;
}
