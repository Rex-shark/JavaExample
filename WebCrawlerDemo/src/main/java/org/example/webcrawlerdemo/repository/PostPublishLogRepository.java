package org.example.webcrawlerdemo.repository;

import org.example.webcrawlerdemo.entity.PostPublishLog;
import org.example.webcrawlerdemo.entity.enums.PublishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 發布記錄 Repository
 */
@Repository
public interface PostPublishLogRepository extends JpaRepository<PostPublishLog, Long> {

    /**
     * 根據 AI 文章 ID 查詢發布記錄
     *
     * @param aiPostId AI 文章 ID
     * @return 發布記錄列表
     */
    List<PostPublishLog> findByAiGeneratedPostId(Long aiPostId);

    /**
     * 根據平台與狀態查詢
     *
     * @param platform 平台
     * @param status   狀態
     * @return 發布記錄列表
     */
    List<PostPublishLog> findByPlatformAndStatus(String platform, PublishStatus status);
}
