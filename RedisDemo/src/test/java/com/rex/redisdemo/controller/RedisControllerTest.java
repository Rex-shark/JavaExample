package com.rex.redisdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rex.redisdemo.request.RedisRequest;
import com.rex.redisdemo.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RedisControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private RedisController redisController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(redisController).build();
    }

    @Test
    void setReturnsOk() throws Exception {
        RedisRequest req = new RedisRequest("k", "v");
        doNothing().when(redisService).set("k","v");

        mockMvc.perform(post("/redis/set")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(redisService).set("k","v");
    }

    @Test
    void getReturnsValue() throws Exception {
        RedisRequest req = new RedisRequest("k", null);
        when(redisService.get("k")).thenReturn(Optional.of("v"));

        mockMvc.perform(post("/redis/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("v"));
    }

    @Test
    void getNotFound() throws Exception {
        RedisRequest req = new RedisRequest("k", null);
        when(redisService.get("k")).thenReturn(Optional.empty());

        mockMvc.perform(post("/redis/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturnsNoContentWhenRemoved() throws Exception {
        RedisRequest req = new RedisRequest("k", null);
        when(redisService.delete("k")).thenReturn(true);

        mockMvc.perform(post("/redis/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundWhenMissing() throws Exception {
        RedisRequest req = new RedisRequest("k", null);
        when(redisService.delete("k")).thenReturn(false);

        mockMvc.perform(post("/redis/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void existsReturnsBoolean() throws Exception {
        RedisRequest req = new RedisRequest("k", null);
        when(redisService.exists("k")).thenReturn(true);

        mockMvc.perform(post("/redis/exists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
