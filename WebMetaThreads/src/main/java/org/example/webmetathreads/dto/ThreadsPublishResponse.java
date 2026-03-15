package org.example.webmetathreads.dto;

import org.example.webmetathreads.entity.ThreadsPost;
import org.example.webmetathreads.entity.enums.PostStatus;

import java.time.LocalDateTime;

/**
 * 發布回應 DTO
 *
 * @param id          資料庫 ID
 * @param containerId Threads API container ID
 * @param mediaId     發布後的 media ID
 * @param status      狀態
 * @param publishedAt 發布時間
 */
public record ThreadsPublishResponse(
        Long id,
        String containerId,
        String mediaId,
        PostStatus status,
        LocalDateTime publishedAt) {
    /**
     * 從 Entity 轉換
     */
    public static ThreadsPublishResponse from(ThreadsPost post) {
        return new ThreadsPublishResponse(
                post.getId(),
                post.getContainerId(),
                post.getMediaId(),
                post.getStatus(),
                post.getPublishedAt());
    }
}
