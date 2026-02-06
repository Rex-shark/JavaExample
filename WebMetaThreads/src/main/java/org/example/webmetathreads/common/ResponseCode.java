package org.example.webmetathreads.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 回應狀態碼枚舉
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客戶端錯誤 4xx
    BAD_REQUEST(400, "請求參數錯誤"),
    UNAUTHORIZED(401, "未授權"),
    FORBIDDEN(403, "禁止存取"),
    NOT_FOUND(404, "資源不存在"),

    // 伺服器錯誤 5xx
    INTERNAL_ERROR(500, "內部伺服器錯誤"),

    // 業務錯誤 6xx
    THREADS_API_ERROR(601, "Threads API 呼叫失敗"),
    THREADS_CONTAINER_NOT_FOUND(602, "Container 不存在"),
    THREADS_POST_NOT_FOUND(603, "發文記錄不存在"),
    THREADS_ALREADY_PUBLISHED(604, "已發布，無法重複發布"),
    THREADS_PUBLISH_FAILED(605, "發布失敗");

    /**
     * 狀態碼
     */
    private final Integer code;

    /**
     * 訊息
     */
    private final String message;
}
