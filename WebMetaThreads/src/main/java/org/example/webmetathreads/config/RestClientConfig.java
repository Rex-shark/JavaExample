package org.example.webmetathreads.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient 配置類別
 */
@Configuration
public class RestClientConfig {

    /**
     * 建立 Threads API 的 RestClient
     *
     * @param properties Threads API 設定
     * @return RestClient
     */
    @Bean
    public RestClient threadsRestClient(ThreadsApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
