package com.rex.specdemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 會員資訊回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    /**
     * UUID（給前端使用）
     */
    private String uuid;

    /**
     * 帳號
     */
    private String username;

    /**
     * 電子郵件
     */
    private String email;

    /**
     * 手機號碼
     */
    private String phone;

    /**
     * 狀態：0-停用，1-啟用
     */
    private Integer status;
}
