package com.rex.linebotdemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface AiChatService {
    String chat(String userMessage) throws JsonProcessingException;
}
