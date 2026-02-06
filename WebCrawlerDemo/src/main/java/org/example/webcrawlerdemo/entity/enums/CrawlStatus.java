package org.example.webcrawlerdemo.entity.enums;

/**
 * 爬蟲文章狀態枚舉
 */
public enum CrawlStatus {
    /**
     * 待處理 - 剛爬取，尚未交給 AI 處理
     */
    PENDING,

    /**
     * 已處理 - AI 已產生文章
     */
    PROCESSED,

    /**
     * 處理失敗
     */
    FAILED
}
