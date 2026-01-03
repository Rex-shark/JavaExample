package org.example.aiollamademo.service;



import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OllamaChatService implements AiChatService {

    private final ChatClient chatClient;


    public OllamaChatService(ChatClient.Builder builder) {
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
