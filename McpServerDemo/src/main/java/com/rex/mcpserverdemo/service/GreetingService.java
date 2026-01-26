package com.rex.mcpserverdemo.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 問候服務 - MCP 工具範例
 * 提供問候語產生和時間查詢功能
 */
@Service
public class GreetingService {

    @Tool(description = "產生個人化的問候語")
    public String greet(
            @ToolParam(description = "要問候的人名") String name) {

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        String timeGreeting;
        if (hour >= 5 && hour < 12) {
            timeGreeting = "早安";
        } else if (hour >= 12 && hour < 18) {
            timeGreeting = "午安";
        } else {
            timeGreeting = "晚安";
        }

        return String.format("%s，%s！很高興見到你！今天過得如何？", timeGreeting, name);
    }

    @Tool(description = "取得目前的日期和時間")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

        String dayOfWeek = switch (now.getDayOfWeek()) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };

        return String.format("目前時間：%s %s", now.format(formatter), dayOfWeek);
    }

    @Tool(description = "產生隨機的勵志語句")
    public String getMotivationalQuote() {
        String[] quotes = {
                "成功不是終點，失敗也不是致命的，重要的是繼續前進的勇氣。",
                "每一個努力的今天，都是未來成功的基石。",
                "不要害怕改變，你可能會失去一些好的東西，但你可能會得到更好的。",
                "夢想不會逃跑，逃跑的永遠是自己。",
                "今天的你，是昨天的選擇；明天的你，是今天的決定。",
                "程式碼是寫給人看的，只是順便讓機器執行。"
        };

        int index = (int) (Math.random() * quotes.length);
        return "💡 今日勵志語：" + quotes[index];
    }
}
