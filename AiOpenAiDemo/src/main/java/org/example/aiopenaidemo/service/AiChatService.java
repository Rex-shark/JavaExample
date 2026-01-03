package org.example.aiopenaidemo.service;

public interface AiChatService {
    String chat(String systemPrompt, String userPrompt, String conversationId);
}
