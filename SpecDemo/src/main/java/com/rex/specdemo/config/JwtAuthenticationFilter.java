package com.rex.specdemo.config;

import com.rex.specdemo.repository.MemberRepository;
import com.rex.specdemo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 認證過濾器
 * 從請求標頭中取得 JWT Token 並驗證
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 取得 Authorization 標頭
        final String authHeader = request.getHeader("Authorization");

        // 檢查是否有 Bearer Token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 取得 Token（移除 "Bearer " 前綴）
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            // 如果有使用者名稱且尚未認證
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 驗證 Token
                if (jwtService.validateToken(jwt, username)) {
                    // 檢查會員是否存在
                    memberRepository.findByUsername(username).ifPresent(member -> {
                        // 建立認證物件
                        UserDetails userDetails = User.builder()
                                .username(member.getUsername())
                                .password(member.getPassword())
                                .authorities(new ArrayList<>())
                                .build();

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 設定到 SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("JWT 認證成功: username={}", username);
                    });
                }
            }
        } catch (Exception e) {
            log.error("JWT 認證失敗: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
