package com.rex.specdemo.controller;

import com.rex.specdemo.dto.request.LoginRequest;
import com.rex.specdemo.dto.request.RegisterRequest;
import com.rex.specdemo.dto.response.ApiResponse;
import com.rex.specdemo.dto.response.LoginResponse;
import com.rex.specdemo.dto.response.MemberResponse;
import com.rex.specdemo.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認證控制器
 * 處理會員註冊與登入相關 API
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "認證管理", description = "會員註冊與登入相關 API")
public class AuthController {

    private final MemberService memberService;

    /**
     * 會員註冊
     */
    @PostMapping("/register")
    @Operation(summary = "會員註冊", description = "新會員註冊，帳號必須唯一")
    public ResponseEntity<ApiResponse<MemberResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("收到註冊請求: username={}", request.getUsername());
        MemberResponse response = memberService.register(request);
        return ResponseEntity.ok(ApiResponse.success("註冊成功", response));
    }

    /**
     * 會員登入
     */
    @PostMapping("/login")
    @Operation(summary = "會員登入", description = "使用帳號密碼登入，成功後回傳 JWT Token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("收到登入請求: username={}", request.getUsername());
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登入成功", response));
    }
}
