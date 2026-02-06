package org.example.webcrawlerdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.webcrawlerdemo.entity.enums.CrawlStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 爬蟲文章 Entity
 * 儲存爬蟲抓取的原始資料
 */
@Entity
@Table(name = "crawl_article", indexes = {
        @Index(name = "idx_crawl_article_status", columnList = "status"),
        @Index(name = "idx_crawl_article_created_at", columnList = "created_at"),
        @Index(name = "idx_crawl_article_composite", columnList = "source_url, title, published_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlArticle extends BaseEntity {

    /**
     * 原始網址 (唯一)
     */
    @Column(name = "source_url", nullable = false, unique = true, length = 500)
    private String sourceUrl;

    /**
     * 來源網站名稱
     */
    @Column(name = "source_name", length = 100)
    private String sourceName;

    /**
     * 原始標題
     */
    @Column(name = "title", length = 500)
    private String title;

    /**
     * 原始內容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 原始作者
     */
    @Column(name = "author", length = 100)
    private String author;

    /**
     * 文章分類
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 原始發布時間
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 處理狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CrawlStatus status = CrawlStatus.PENDING;

    /**
     * 關聯的 AI 生成文章
     */
    @OneToMany(mappedBy = "crawlArticle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AiGeneratedPost> aiGeneratedPosts = new ArrayList<>();
}
