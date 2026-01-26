package com.rex.mcpserverdemo.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * 計算機服務 - MCP 工具範例
 * 提供基本的數學運算功能
 */
@Service
public class CalculatorService {

    @Tool(description = "計算兩個數字相加")
    public double add(
            @ToolParam(description = "第一個數字") double a,
            @ToolParam(description = "第二個數字") double b) {
        return a + b;
    }

    @Tool(description = "計算兩個數字相減")
    public double subtract(
            @ToolParam(description = "被減數") double a,
            @ToolParam(description = "減數") double b) {
        return a - b;
    }

    @Tool(description = "計算兩個數字相乘")
    public double multiply(
            @ToolParam(description = "第一個數字") double a,
            @ToolParam(description = "第二個數字") double b) {
        return a * b;
    }

    @Tool(description = "計算兩個數字相除")
    public double divide(
            @ToolParam(description = "被除數") double a,
            @ToolParam(description = "除數") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除數不能為零");
        }
        return a / b;
    }
}
