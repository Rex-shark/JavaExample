package com.rex.mcpserverdemo.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

/**
 * 天氣服務 - MCP 工具範例
 * 提供模擬的天氣查詢功能
 */
@Service
public class WeatherService {

    private final Random random = new Random();

    // 模擬城市天氣資料
    private final Map<String, String> weatherConditions = Map.of(
            "台北", "多雲",
            "台中", "晴天",
            "高雄", "晴時多雲",
            "台南", "晴天",
            "新竹", "陰天",
            "Tokyo", "晴天",
            "Osaka", "多雲");

    @Tool(description = "查詢指定城市的目前天氣狀況")
    public String getWeather(
            @ToolParam(description = "要查詢天氣的城市名稱，例如：台北、東京") String city) {

        String condition = weatherConditions.getOrDefault(city, "晴天");
        int temperature = 15 + random.nextInt(20); // 15-34度
        int humidity = 40 + random.nextInt(50); // 40-89%

        return String.format(
                "【%s 天氣資訊】\n" +
                        "天氣狀況：%s\n" +
                        "目前溫度：%d°C\n" +
                        "相對濕度：%d%%\n" +
                        "（此為模擬資料）",
                city, condition, temperature, humidity);
    }

    @Tool(description = "查詢指定城市未來幾天的天氣預報")
    public String getForecast(
            @ToolParam(description = "要查詢天氣預報的城市名稱") String city,
            @ToolParam(description = "要查詢的天數（1-7天）") int days) {

        if (days < 1 || days > 7) {
            return "請輸入 1-7 天的預報天數";
        }

        StringBuilder forecast = new StringBuilder();
        forecast.append(String.format("【%s %d 日天氣預報】\n", city, days));
        forecast.append("─".repeat(20)).append("\n");

        String[] conditions = { "晴天", "多雲", "陰天", "小雨", "晴時多雲" };

        for (int i = 1; i <= days; i++) {
            String condition = conditions[random.nextInt(conditions.length)];
            int highTemp = 20 + random.nextInt(15);
            int lowTemp = highTemp - 5 - random.nextInt(5);

            forecast.append(String.format("第 %d 天：%s，%d°C ~ %d°C\n",
                    i, condition, lowTemp, highTemp));
        }

        forecast.append("（此為模擬資料）");
        return forecast.toString();
    }
}
