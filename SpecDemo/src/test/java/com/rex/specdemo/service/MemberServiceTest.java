package com.rex.specdemo.service;

import com.rex.specdemo.dto.request.LoginRequest;
import com.rex.specdemo.dto.request.RegisterRequest;
import com.rex.specdemo.dto.response.LoginResponse;
import com.rex.specdemo.dto.response.MemberResponse;
import com.rex.specdemo.entity.Member;
import com.rex.specdemo.exception.BusinessException;
import com.rex.specdemo.repository.MemberRepository;
import com.rex.specdemo.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * MemberService 單元測試
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private MemberServiceImpl memberService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Member testMember;

    @BeforeEach
    void setUp() {
        // 準備測試資料
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        testMember = Member.builder()
                .id(1L)
                .uuid(UUID.randomUUID().toString())
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .email("test@example.com")
                .status(1)
                .build();
    }

    @Test
    @DisplayName("會員註冊 - 成功")
    void register_Success() {
        // Arrange
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setId(1L);
            member.setUuid(UUID.randomUUID().toString());
            return member;
        });

        // Act
        MemberResponse response = memberService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("會員註冊 - 帳號已存在")
    void register_UsernameExists_ThrowsException() {
        // Arrange
        when(memberRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.register(registerRequest));
        assertEquals("帳號已被使用", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("會員登入 - 成功")
    void login_Success() {
        // Arrange
        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("password123", testMember.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testMember)).thenReturn("jwt.token.here");
        when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

        // Act
        LoginResponse response = memberService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt.token.here", response.getAccessToken());
        assertEquals(86400L, response.getExpiresIn());
    }

    @Test
    @DisplayName("會員登入 - 帳號不存在")
    void login_UsernameNotFound_ThrowsException() {
        // Arrange
        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.login(loginRequest));
        assertEquals("帳號或密碼錯誤", exception.getMessage());
        assertEquals(401, exception.getCode());
    }

    @Test
    @DisplayName("會員登入 - 密碼錯誤")
    void login_WrongPassword_ThrowsException() {
        // Arrange
        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("password123", testMember.getPassword())).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.login(loginRequest));
        assertEquals("帳號或密碼錯誤", exception.getMessage());
        assertEquals(401, exception.getCode());
    }

    @Test
    @DisplayName("會員登入 - 帳號已停用")
    void login_AccountDisabled_ThrowsException() {
        // Arrange
        testMember.setStatus(0);
        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("password123", testMember.getPassword())).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.login(loginRequest));
        assertEquals("帳號已被停用", exception.getMessage());
        assertEquals(403, exception.getCode());
    }
}
