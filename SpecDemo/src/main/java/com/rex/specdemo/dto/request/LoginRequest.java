package com.rex.specdemo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 會員登入請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 帳號（必填）
     */
    @NotBlank(message = "帳號不可為空")
    private String username;

    /**
     * 密碼（必填）
     */
    @NotBlank(message = "密碼不可為空")
    private String password;
}
