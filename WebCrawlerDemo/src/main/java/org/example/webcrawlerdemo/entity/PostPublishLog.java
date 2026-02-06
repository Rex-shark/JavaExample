package org.example.webcrawlerdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.webcrawlerdemo.entity.enums.PublishStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 發布記錄 Entity
 * 記錄文章發布至各平台的結果
 */
@Entity
@Table(name = "post_publish_log", indexes = {
        @Index(name = "idx_publish_log_ai_post_id", columnList = "ai_post_id"),
        @Index(name = "idx_publish_log_platform", columnList = "platform"),
        @Index(name = "idx_publish_log_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostPublishLog {

    /**
     * 主鍵
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 關聯的 AI 生成文章
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_post_id", nullable = false)
    private AiGeneratedPost aiGeneratedPost;

    /**
     * 發布平台
     */
    @Column(name = "platform", nullable = false, length = 50)
    private String platform;

    /**
     * 外部平台的文章 ID
     */
    @Column(name = "external_post_id", length = 100)
    private String externalPostId;

    /**
     * 發布狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PublishStatus status;

    /**
     * 錯誤訊息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 發布時間
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 建立時間
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
