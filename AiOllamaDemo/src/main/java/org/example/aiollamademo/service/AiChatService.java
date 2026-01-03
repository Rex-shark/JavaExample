package org.example.aiollamademo.service;

public interface AiChatService {
    String chat(String systemPrompt, String userPrompt, String conversationId);
}
