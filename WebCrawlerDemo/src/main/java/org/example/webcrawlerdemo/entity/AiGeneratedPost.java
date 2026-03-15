package org.example.webcrawlerdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.webcrawlerdemo.entity.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 生成文章 Entity
 * 儲存 AI 產生的文章內容
 */
@Entity
@Table(name = "ai_generated_post", indexes = {
        @Index(name = "idx_ai_post_status", columnList = "status"),
        @Index(name = "idx_ai_post_crawl_article_id", columnList = "crawl_article_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiGeneratedPost extends BaseEntity {

    /**
     * 關聯的爬蟲文章
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawl_article_id", nullable = false)
    private CrawlArticle crawlArticle;

    /**
     * 使用的 AI 模型
     */
    @Column(name = "ai_model", length = 50)
    private String aiModel;

    /**
     * AI 產生的標題
     */
    @Column(name = "generated_title", length = 500)
    private String generatedTitle;

    /**
     * AI 產生的內容
     */
    @Column(name = "generated_content", columnDefinition = "TEXT")
    private String generatedContent;

    /**
     * 使用的 Prompt
     */
    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed;

    /**
     * 文章狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    /**
     * 重試次數
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 生成時間
     */
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    /**
     * 關聯的發布記錄
     */
    @OneToMany(mappedBy = "aiGeneratedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostPublishLog> publishLogs = new ArrayList<>();
}
