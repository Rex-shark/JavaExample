package org.example.webmetathreads.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.webmetathreads.entity.enums.PostStatus;

import java.time.LocalDateTime;

/**
 * Threads 發文記錄 Entity
 * 模擬 AI 生成文章發布到 Threads
 */
@Entity
@Table(name = "threads_post", indexes = {
        @Index(name = "idx_threads_post_status", columnList = "status"),
        @Index(name = "idx_threads_post_container_id", columnList = "container_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadsPost extends BaseEntity {

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
     * Threads API 回傳的 container ID
     */
    @Column(name = "container_id", length = 100)
    private String containerId;

    /**
     * 發布後的 media ID
     */
    @Column(name = "media_id", length = 100)
    private String mediaId;

    /**
     * 發文狀態
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
     * 發布時間
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 錯誤訊息
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
