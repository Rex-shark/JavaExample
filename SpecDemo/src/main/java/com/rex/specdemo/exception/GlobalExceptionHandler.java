package com.rex.specdemo.exception;

import com.rex.specdemo.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域例外處理器
 * 使用 @RestControllerAdvice 統一處理 Controller 層的例外
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理業務邏輯例外
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("業務邏輯錯誤: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    /**
     * 處理參數驗證失敗
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("參數驗證失敗: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "參數驗證失敗"));
    }

    /**
     * 處理認證失敗（帳號密碼錯誤）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("認證失敗: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "帳號或密碼錯誤"));
    }

    /**
     * 處理認證相關例外
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("認證錯誤: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "認證失敗"));
    }

    /**
     * 處理所有未知例外
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("系統錯誤: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "系統發生錯誤，請稍後再試"));
    }
}
