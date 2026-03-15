package org.example.webmetathreads.dto;

import org.example.webmetathreads.entity.ThreadsPost;
import org.example.webmetathreads.entity.enums.PostStatus;

import java.time.LocalDateTime;

/**
 * Container 回應 DTO
 *
 * @param id               資料庫 ID
 * @param containerId      Threads API container ID
 * @param aiModel          AI 模型
 * @param generatedTitle   AI 產生的標題
 * @param generatedContent AI 產生的內容
 * @param status           狀態
 * @param createdAt        建立時間
 */
public record ThreadsContainerResponse(
        Long id,
        String containerId,
        String aiModel,
        String generatedTitle,
        String generatedContent,
        PostStatus status,
        LocalDateTime createdAt) {
    /**
     * 從 Entity 轉換
     */
    public static ThreadsContainerResponse from(ThreadsPost post) {
        return new ThreadsContainerResponse(
                post.getId(),
                post.getContainerId(),
                post.getAiModel(),
                post.getGeneratedTitle(),
                post.getGeneratedContent(),
                post.getStatus(),
                post.getCreatedAt());
    }
}
