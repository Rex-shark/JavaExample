package org.example.webmetathreads.common;

import lombok.Getter;

/**
 * 業務例外類別
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 回應狀態碼
     */
    private final ResponseCode responseCode;

    /**
     * 建構子
     *
     * @param responseCode 回應狀態碼
     */
    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    /**
     * 建構子（自訂訊息）
     *
     * @param responseCode 回應狀態碼
     * @param message      自訂訊息
     */
    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }
}
