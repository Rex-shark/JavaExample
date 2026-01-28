package com.rex.specdemo.exception;

import lombok.Getter;

/**
 * 業務邏輯例外
 * 用於處理可預期的業務錯誤情況
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 錯誤代碼
     */
    private final Integer code;

    /**
     * 建構子（預設錯誤碼 400）
     *
     * @param message 錯誤訊息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 建構子（自訂錯誤碼）
     *
     * @param code    錯誤代碼
     * @param message 錯誤訊息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
