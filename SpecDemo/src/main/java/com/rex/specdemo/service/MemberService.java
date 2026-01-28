package com.rex.specdemo.service;

import com.rex.specdemo.dto.request.LoginRequest;
import com.rex.specdemo.dto.request.RegisterRequest;
import com.rex.specdemo.dto.response.LoginResponse;
import com.rex.specdemo.dto.response.MemberResponse;

/**
 * 會員服務介面
 */
public interface MemberService {

    /**
     * 會員註冊
     *
     * @param request 註冊請求
     * @return 會員資訊
     */
    MemberResponse register(RegisterRequest request);

    /**
     * 會員登入
     *
     * @param request 登入請求
     * @return 登入回應（包含 Token）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 依帳號查詢會員資訊
     *
     * @param username 帳號
     * @return 會員資訊
     */
    MemberResponse findByUsername(String username);
}
