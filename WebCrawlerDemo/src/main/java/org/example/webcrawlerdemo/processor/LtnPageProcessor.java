package org.example.webcrawlerdemo.processor;

import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自由時報新聞爬蟲處理器
 * 負責從自由時報網站擷取新聞內容
 */
@Slf4j
public class LtnPageProcessor implements PageProcessor {

    /**
     * 網站配置
     */
    private final Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(2000)
            .setTimeOut(15000)
            .setUserAgent(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7");

    /**
     * 處理網頁，擷取自由時報新聞內容
     *
     * @param page WebMagic Page 物件
     */
    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        log.info("正在處理自由時報新聞: {}", url);

        // 擷取標題
        String title = page.getHtml().css("h1", "text").get();
        if (title == null) {
            title = page.getHtml().xpath("//h1/text()").get();
        }

        // 擷取作者 (從 article_edit 中提取)
        String author = page.getHtml().css("span.article_edit", "text").get();
        if (author == null) {
            author = page.getHtml().css("span.writer", "text").get();
        }
        if (author == null) {
            author = page.getHtml().css(".auther", "text").get();
        }

        // 擷取發布時間 (從 article_time 中提取)
        String publishTime = page.getHtml().css("span.article_time", "text").get();
        if (publishTime == null) {
            publishTime = page.getHtml().css("span.time", "text").get();
        }

        // 擷取文章內容
        List<String> paragraphs = page.getHtml().css("div.text p", "allText").all();
        if (paragraphs.isEmpty()) {
            paragraphs = page.getHtml().css("div.content p", "allText").all();
        }
        if (paragraphs.isEmpty()) {
            paragraphs = page.getHtml().css("article p", "allText").all();
        }

        String content = String.join("\n\n", paragraphs);

        // 如果內容太長，截取前 3000 字元
        if (content.length() > 3000) {
            content = content.substring(0, 3000) + "\n...(內容已截斷)";
        }

        // 擷取分類
        String category = null;

        // 方式 1：從麵包屑抓取中文分類（如：地方、政治、生活）
        category = page.getHtml().css(".breadcrumbs a.boxText", "text").get();

        // 方式 2（備選）：從 meta tag 抓取英文分類（如：local, politics, life）
        if (category == null || category.trim().isEmpty()) {
            category = page.getHtml().xpath("//meta[@property='article:section']/@content").get();
        }

        // 方式 3（備選）：從 URL 解析英文分類
        if (category == null || category.trim().isEmpty()) {
            // URL 格式：https://news.ltn.com.tw/news/{category}/paper/{id}
            String urlStr = url; // 使用已經取得的 url 字串
            String[] parts = urlStr.split("/");
            if (parts.length >= 5) {
                category = parts[4]; // 取得 category 部分（索引4：news/{category}/paper）
            }
        }

        // 放入結果
        page.putField("title", title != null ? title.trim() : "無標題");
        page.putField("url", url);
        page.putField("author", author != null ? author.trim() : "自由時報");
        page.putField("publishTime", publishTime);
        page.putField("category", category != null ? category.trim() : null);
        page.putField("content", content.trim());
        page.putField("crawlTime", LocalDateTime.now());

        log.info("文章處理完成: {}", title);
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
