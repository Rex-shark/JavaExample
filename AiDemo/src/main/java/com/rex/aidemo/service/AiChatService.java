package com.rex.aidemo.service;

import org.springframework.ai.chat.client.ChatClient;

public interface AiChatService {
    String chat(String systemPrompt, String userPrompt, String conversationId);
}
