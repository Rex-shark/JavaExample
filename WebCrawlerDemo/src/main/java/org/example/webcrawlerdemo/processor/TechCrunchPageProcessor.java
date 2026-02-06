package org.example.webcrawlerdemo.processor;

import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TechCrunch 新聞爬蟲處理器
 * 負責從 TechCrunch 網站擷取最新新聞文章
 */
@Slf4j
public class TechCrunchPageProcessor implements PageProcessor {

    /**
     * 已處理的文章數量計數器
     */
    private final AtomicInteger articleCount = new AtomicInteger(0);

    /**
     * 最大文章數量限制
     */
    private final int maxArticles;

    /**
     * 網站配置
     */
    private final Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(2000) // 禮貌爬取，間隔 2 秒
            .setTimeOut(15000)
            .setUserAgent(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "en-US,en;q=0.9");

    /**
     * 建構子
     *
     * @param maxArticles 最大文章數量限制
     */
    public TechCrunchPageProcessor(int maxArticles) {
        this.maxArticles = maxArticles;
    }

    /**
     * 預設建構子，預設抓取 5 篇文章
     */
    public TechCrunchPageProcessor() {
        this(5);
    }

    /**
     * 處理網頁，根據 URL 判斷為列表頁或文章頁
     *
     * @param page WebMagic Page 物件
     */
    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        log.info("正在處理頁面: {}", url);

        // 判斷是列表頁還是文章頁
        if (url.contains("/latest/") || url.equals("https://techcrunch.com/latest/")) {
            processListPage(page);
        } else {
            processArticlePage(page);
        }
    }

    /**
     * 處理列表頁，擷取文章連結並加入爬取佇列
     *
     * @param page WebMagic Page 物件
     */
    private void processListPage(Page page) {
        log.info("處理列表頁，擷取文章連結...");

        // 擷取所有文章連結 (TechCrunch 的文章連結格式)
        List<String> articleLinks = page.getHtml()
                .css("h3 a", "href")
                .all();

        if (articleLinks.isEmpty()) {
            // 備用選擇器
            articleLinks = page.getHtml()
                    .xpath("//a[contains(@href, '/2026/') or contains(@href, '/2025/')]/@href")
                    .all();
        }

        // 過濾並限制文章數量
        int count = 0;
        for (String link : articleLinks) {
            if (count >= maxArticles)
                break;

            // 只處理文章連結 (包含日期格式的 URL)
            if (link.contains("techcrunch.com/2") && !link.contains("/author/") && !link.contains("/category/")) {
                page.addTargetRequest(link);
                count++;
                log.info("加入待爬取: {}", link);
            }
        }

        // 列表頁本身不產生輸出
        page.setSkip(true);
        log.info("已加入 {} 篇文章待爬取", count);
    }

    /**
     * 處理文章頁，擷取標題、作者、內容等詳細資訊
     *
     * @param page WebMagic Page 物件
     */
    private void processArticlePage(Page page) {
        int currentCount = articleCount.incrementAndGet();
        log.info("處理第 {} 篇文章: {}", currentCount, page.getUrl());

        // 擷取標題
        String title = page.getHtml().css("h1", "text").get();
        if (title == null) {
            title = page.getHtml().xpath("//h1/text()").get();
        }

        // 擷取作者
        String author = page.getHtml().css("a[href*='/author/']", "text").get();

        // 擷取發布時間
        String publishTime = page.getHtml().css("time", "datetime").get();

        // 擷取文章內容（使用 allText 以包含 <a> 等子元素內的文字）
        List<String> paragraphs = page.getHtml().css("div.entry-content p", "allText").all();
        if (paragraphs.isEmpty()) {
            paragraphs = page.getHtml().css("article p", "allText").all();
        }
        if (paragraphs.isEmpty()) {
            paragraphs = page.getHtml().css(".article-content p", "allText").all();
        }

        String content = String.join("\n\n", paragraphs);

        // 如果內容太長，截取前 2000 字元
        if (content.length() > 2000) {
            content = content.substring(0, 2000) + "\n...(內容已截斷)";
        }

        // 擷取分類（優先使用文章主分類）
        String category = page.getHtml().css("a.wp-block-tenup-post-primary-term", "text").get();
        // 備援方案：使用 meta 標籤
        if (category == null || category.isBlank()) {
            category = page.getHtml().xpath("//meta[@name='parsely-section']/@content").get();
        }
        // 第三備援：使用 article-hero__category
        if (category == null || category.isBlank()) {
            category = page.getHtml().css(".article-hero__category a", "text").get();
        }

        // 放入結果
        page.putField("title", title != null ? title.trim() : "無標題");
        page.putField("url", page.getUrl().toString());
        page.putField("author", author != null ? author.trim() : "未知作者");
        page.putField("publishTime", publishTime);
        page.putField("category", category != null ? category.trim() : null);
        page.putField("content", content.trim());
        page.putField("crawlTime", LocalDateTime.now());
        page.putField("articleIndex", currentCount);

        log.info("文章處理完成: {} by {} [{}]", title, author, category);
    }

    /**
     * 取得網站配置
     *
     * @return Site 配置物件
     */
    @Override
    public Site getSite() {
        return site;
    }
}
