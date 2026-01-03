package org.example.aiollamademo.controller;

import jakarta.annotation.Resource;
import org.example.aiollamademo.dto.request.ChatRequest;
import org.example.aiollamademo.dto.respones.AiChatResponse;
import org.example.aiollamademo.service.OllamaChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@RestController
@RequestMapping("/api/ollama")
public class OllamaChatController {

    @Resource
    private OllamaChatService ollamaChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody ChatRequest request) {
        String nowDate = java.time.LocalDate.now().toString();


        String nowDate2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        System.out.println("nowDate2 = " + nowDate2);


        String userId = "Rex";
        String systemPrompt = "你是一個TIST尾牙機器人、說話風格簡潔俏皮有趣、使用繁體中文，台灣用語。" +
                "現在台灣時間(UTC+8): " + nowDate + "。" +
                "如果使用者詢問關於時間、日期或節日的問題，請根據上述時間資訊提供準確的回答。"+
                "回覆格式不要使用markdown語法，使用適合即時通訊平台line的文字格式。";

        String reply = ollamaChatService.chat( systemPrompt,request.prompt(),userId);

        return ResponseEntity.ok(new AiChatResponse(reply));
    }
}
