package org.example.webmetathreads.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 統一 API 回應格式
 *
 * @param <T> 資料類型
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
     * 時間戳記
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 追蹤 ID
     */
    @Builder.Default
    private String traceId = UUID.randomUUID().toString().substring(0, 8);

    /**
     * 成功回應（無資料）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    /**
     * 成功回應（含資料）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    /**
     * 成功回應（自訂訊息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失敗回應
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .build();
    }

    /**
     * 失敗回應（自訂訊息）
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(message)
                .build();
    }
}
