package com.rex.mcpserverdemo.config;

import com.rex.mcpserverdemo.service.CalculatorService;
import com.rex.mcpserverdemo.service.GreetingService;
import com.rex.mcpserverdemo.service.SkillReaderService;
import com.rex.mcpserverdemo.service.UserService;
import com.rex.mcpserverdemo.service.WeatherService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 工具設定類別
 * 註冊所有 MCP 工具服務
 */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider calculatorTools(CalculatorService calculatorService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(calculatorService)
                .build();
    }

    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherService)
                .build();
    }

    @Bean
    public ToolCallbackProvider greetingTools(GreetingService greetingService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(greetingService)
                .build();
    }

    @Bean
    public ToolCallbackProvider userTools(UserService userService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(userService)
                .build();
    }

    @Bean
    public ToolCallbackProvider skillReaderTools(SkillReaderService skillReaderService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(skillReaderService)
                .build();
    }
}
