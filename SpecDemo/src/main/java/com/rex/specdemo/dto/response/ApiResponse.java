package com.rex.specdemo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統一 API 回應格式
 *
 * @param <T> 資料型別
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 狀態碼
     */
    private Integer code;

    /**
     * 訊息
     */
    private String message;

    /**
     * 資料
     */
    private T data;

    /**
     * 成功回應（帶資料）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .build();
    }

    /**
     * 成功回應（帶訊息與資料）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 成功回應（僅訊息）
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .build();
    }

    /**
     * 失敗回應
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 失敗回應（預設 400）
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(400, message);
    }
}
