package com.rex.aidemo.controller;

import com.rex.aidemo.dto.ChatRequest;
import com.rex.aidemo.dto.ChatResponse;
import com.rex.aidemo.dto.ChatSystemPromptRequest;
import com.rex.aidemo.service.GenAiChatService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/genai")
public class GenAiChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Resource
    private ChatMemory chatMemory;

    public GenAiChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }
    @Resource
    private GenAiChatService genAiChatService;


    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        // 基本聊天，現在也會參考 VectorStore 中的資料
        String reply = chatClient.prompt().user(request.prompt()).call().content();
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @PostMapping("/chat2")
    public ResponseEntity<ChatResponse> chatWithSystem(@RequestBody ChatSystemPromptRequest request) {
        String nowDate = java.time.LocalDate.now().toString();
        String userId = request.system();
        String systemPrompt = "你是一個TIST尾牙機器人、說話風格簡潔俏皮有趣、使用繁體中文，台灣用語。" +
                "現在時間: " + nowDate + "。" +
                "如果使用者詢問關於時間、日期或節日的問題，請根據上述時間資訊提供準確的回答。"+
                "回覆格式不要使用markdown語法，使用適合即時通訊平台line的文字格式。";

        String reply = genAiChatService.chat( systemPrompt,request.prompt(),userId);

        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
