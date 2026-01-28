package com.rex.specdemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 會員登入回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 會員 UUID（給前端使用的唯一識別碼）
     */
    private String uuid;

    /**
     * 帳號
     */
    private String username;

    /**
     * JWT Access Token
     */
    private String accessToken;

    /**
     * Token 過期時間（秒）
     */
    private Long expiresIn;
}
