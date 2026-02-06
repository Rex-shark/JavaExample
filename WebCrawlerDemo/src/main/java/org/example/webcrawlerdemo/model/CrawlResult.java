package org.example.webcrawlerdemo.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 爬蟲結果資料模型
 */
@Data
@Builder
public class CrawlResult {

    /**
     * 網頁標題
     */
    private String title;

    /**
     * 網頁 URL
     */
    private String url;

    /**
     * 網頁內容摘要
     */
    private String content;

    /**
     * 擷取的連結列表
     */
    private List<String> links;

    /**
     * 爬取時間
     */
    private LocalDateTime crawlTime;

    /**
     * 格式化輸出
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("標題: ").append(title).append("\n");
        sb.append("URL: ").append(url).append("\n");
        sb.append("爬取時間: ").append(crawlTime).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("內容:\n").append(content).append("\n");
        if (links != null && !links.isEmpty()) {
            sb.append("----------------------------------------\n");
            sb.append("連結數量: ").append(links.size()).append("\n");
        }
        sb.append("========================================\n");
        return sb.toString();
    }
}
