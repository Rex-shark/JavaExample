package org.example.webcrawlerdemo.repository;

import org.example.webcrawlerdemo.entity.CrawlArticle;
import org.example.webcrawlerdemo.entity.enums.CrawlStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 爬蟲文章 Repository
 */
@Repository
public interface CrawlArticleRepository extends JpaRepository<CrawlArticle, Long> {

    /**
     * 根據原始網址查詢文章
     *
     * @param sourceUrl 原始網址
     * @return 文章 Optional
     */
    Optional<CrawlArticle> findBySourceUrl(String sourceUrl);

    /**
     * 檢查網址是否已存在
     *
     * @param sourceUrl 原始網址
     * @return 是否存在
     */
    boolean existsBySourceUrl(String sourceUrl);

    /**
     * 檢查文章是否已存在（組合鍵：URL + 標題 + 發布時間）
     *
     * @param sourceUrl   原始網址
     * @param title       標題
     * @param publishedAt 發布時間
     * @return 是否存在
     */
    boolean existsBySourceUrlAndTitleAndPublishedAt(String sourceUrl, String title, LocalDateTime publishedAt);

    /**
     * 查詢指定來源的最新一筆文章（根據來源 URL 降序排序）
     *
     * @param sourceName 來源名稱（如：自由時報）
     * @return 最新的文章（Optional）
     */
    Optional<CrawlArticle> findFirstBySourceNameOrderBySourceUrlDesc(String sourceName);

    /**
     * 根據狀態查詢文章列表
     *
     * @param status 狀態
     * @return 文章列表
     */
    List<CrawlArticle> findByStatus(CrawlStatus status);

    /**
     * 查詢待處理的文章列表
     *
     * @return 待處理文章列表
     */
    default List<CrawlArticle> findPendingArticles() {
        return findByStatus(CrawlStatus.PENDING);
    }
}
