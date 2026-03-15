package org.example.webcrawlerdemo.repository;

import org.example.webcrawlerdemo.entity.AiGeneratedPost;
import org.example.webcrawlerdemo.entity.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI 生成文章 Repository
 */
@Repository
public interface AiGeneratedPostRepository extends JpaRepository<AiGeneratedPost, Long> {

    /**
     * 根據狀態查詢文章列表
     *
     * @param status 狀態
     * @return 文章列表
     */
    List<AiGeneratedPost> findByStatus(PostStatus status);

    /**
     * 查詢待發布的文章列表
     *
     * @return 待發布文章列表
     */
    default List<AiGeneratedPost> findDraftPosts() {
        return findByStatus(PostStatus.DRAFT);
    }

    /**
     * 根據爬蟲文章 ID 查詢
     *
     * @param crawlArticleId 爬蟲文章 ID
     * @return 生成的文章列表
     */
    List<AiGeneratedPost> findByCrawlArticleId(Long crawlArticleId);
}
