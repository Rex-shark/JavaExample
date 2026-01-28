package com.rex.specdemo.service.impl;

import com.rex.specdemo.dto.request.LoginRequest;
import com.rex.specdemo.dto.request.RegisterRequest;
import com.rex.specdemo.dto.response.LoginResponse;
import com.rex.specdemo.dto.response.MemberResponse;
import com.rex.specdemo.entity.Member;
import com.rex.specdemo.exception.BusinessException;
import com.rex.specdemo.repository.MemberRepository;
import com.rex.specdemo.service.JwtService;
import com.rex.specdemo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 會員服務實作類別
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 會員註冊
     * 1. 檢查帳號是否重複
     * 2. 密碼使用 BCrypt 加密
     * 3. 自動產生 UUID
     * 4. 儲存會員資料
     */
    @Override
    @Transactional
    public MemberResponse register(RegisterRequest request) {
        // 檢查帳號是否已存在
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("帳號已被使用");
        }

        // 檢查 Email 是否已存在（若有填寫）
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("電子郵件已被使用");
        }

        // 建立會員實體
        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(1)
                .build();

        // 儲存會員
        Member savedMember = memberRepository.save(member);
        log.info("會員註冊成功: username={}, uuid={}", savedMember.getUsername(), savedMember.getUuid());

        return toMemberResponse(savedMember);
    }

    /**
     * 會員登入
     * 1. 查詢會員（依帳號）
     * 2. 驗證密碼
     * 3. 產生 JWT Token
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查詢會員
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "帳號或密碼錯誤"));

        // 驗證密碼
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(401, "帳號或密碼錯誤");
        }

        // 檢查會員狀態
        if (member.getStatus() != 1) {
            throw new BusinessException(403, "帳號已被停用");
        }

        // 產生 JWT Token
        String token = jwtService.generateToken(member);
        log.info("會員登入成功: username={}", member.getUsername());

        return LoginResponse.builder()
                .uuid(member.getUuid())
                .username(member.getUsername())
                .accessToken(token)
                .expiresIn(jwtService.getExpirationInSeconds())
                .build();
    }

    /**
     * 依帳號查詢會員資訊
     */
    @Override
    public MemberResponse findByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("會員不存在"));
        return toMemberResponse(member);
    }

    /**
     * 轉換為回應 DTO
     */
    private MemberResponse toMemberResponse(Member member) {
        return MemberResponse.builder()
                .uuid(member.getUuid())
                .username(member.getUsername())
                .email(member.getEmail())
                .phone(member.getPhone())
                .status(member.getStatus())
                .build();
    }
}
