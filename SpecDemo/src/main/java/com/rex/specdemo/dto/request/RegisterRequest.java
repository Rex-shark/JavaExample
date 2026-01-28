package com.rex.specdemo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 會員註冊請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * 帳號（必填，4-50 字元）
     */
    @NotBlank(message = "帳號不可為空")
    @Size(min = 4, max = 50, message = "帳號長度需為 4-50 字元")
    private String username;

    /**
     * 密碼（必填，6-100 字元）
     */
    @NotBlank(message = "密碼不可為空")
    @Size(min = 6, max = 100, message = "密碼長度需為 6-100 字元")
    private String password;

    /**
     * 電子郵件（選填）
     */
    private String email;

    /**
     * 手機號碼（選填）
     */
    private String phone;
}
