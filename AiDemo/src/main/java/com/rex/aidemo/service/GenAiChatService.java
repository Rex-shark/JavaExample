package com.rex.aidemo.service;

import com.rex.aidemo.AiDemoApplication;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

@Service
public class GenAiChatService implements AiChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Resource
    private ChatMemory chatMemory;

    public GenAiChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public String chat(String systemPrompt, String userPrompt, String conversationId) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .advisors(
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(conversationId)
                                .build()
                )
                .call()
                .content();
    }

}
