package org.example.aiopenaidemo.service;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OpenAiChatService implements AiChatService {

    private final ChatClient chatClient;


    public OpenAiChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String chat(String systemPrompt, String userPrompt, String conversationId) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

}
