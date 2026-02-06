package org.example.webmetathreads.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.webmetathreads.common.ApiResponse;
import org.example.webmetathreads.common.BusinessException;
import org.example.webmetathreads.common.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全域例外處理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理業務例外
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("業務例外: {}", e.getMessage());
        return ApiResponse.error(e.getResponseCode(), e.getMessage());
    }

    /**
     * 處理驗證例外
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("驗證失敗: {}", errors);
        return ApiResponse.error(ResponseCode.BAD_REQUEST, errors);
    }

    /**
     * 處理其他例外
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系統錯誤: {}", e.getMessage(), e);
        return ApiResponse.error(ResponseCode.INTERNAL_ERROR, e.getMessage());
    }
}
