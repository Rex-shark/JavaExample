package com.rex.specdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rex.specdemo.dto.request.LoginRequest;
import com.rex.specdemo.dto.request.RegisterRequest;
import com.rex.specdemo.dto.response.LoginResponse;
import com.rex.specdemo.dto.response.MemberResponse;
import com.rex.specdemo.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 整合測試
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("POST /api/auth/register - 註冊成功")
    void register_Success() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        MemberResponse response = MemberResponse.builder()
                .uuid("test-uuid")
                .username("testuser")
                .status(1)
                .build();

        when(memberService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("註冊成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 參數驗證失敗")
    void register_ValidationFailed() throws Exception {
        // Arrange - 帳號太短
        RegisterRequest request = RegisterRequest.builder()
                .username("abc") // 小於 4 字元
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - 登入成功")
    void login_Success() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        LoginResponse response = LoginResponse.builder()
                .uuid("test-uuid")
                .username("testuser")
                .accessToken("jwt.token.here")
                .expiresIn(86400L)
                .build();

        when(memberService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登入成功"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt.token.here"))
                .andExpect(jsonPath("$.data.expiresIn").value(86400));
    }
}
