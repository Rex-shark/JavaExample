package org.example.aiopenaidemo.controller;

import jakarta.annotation.Resource;
import org.example.aiopenaidemo.dto.request.ChatRequest;
import org.example.aiopenaidemo.dto.respones.AiChatResponse;
import org.example.aiopenaidemo.service.OpenAiChatService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/openai")
public class OpenAiChatController {

    @Resource
    private OpenAiChatService openAiChatService;

    @PostMapping("/rag")
    public ResponseEntity<AiChatResponse> rag(@RequestBody ChatRequest request) {
        String nowDate = java.time.LocalDate.now().toString();
        String userId = "Rex";
        String systemPrompt = "你是一個TIST尾牙機器人、說話風格簡潔俏皮有趣、使用繁體中文，台灣用語。" +
                "現在時間: " + nowDate + "。" +
                "如果使用者詢問關於時間、日期或節日的問題，請根據上述時間資訊提供準確的回答。"+
                "回覆格式不要使用markdown語法，使用適合即時通訊平台line的文字格式。";

        String reply = "";

        return ResponseEntity.ok(new AiChatResponse(reply));
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody ChatRequest request) {
        String nowDate = java.time.LocalDate.now().toString();
        String userId = "Rex";
        String systemPrompt = "你是一個TIST尾牙機器人、說話風格簡潔俏皮有趣、使用繁體中文，台灣用語。" +
                "現在時間: " + nowDate + "。" +
                "如果使用者詢問關於時間、日期或節日的問題，請根據上述時間資訊提供準確的回答。"+
                "回覆格式不要使用markdown語法，使用適合即時通訊平台line的文字格式。";

        String reply = openAiChatService.chat( systemPrompt,request.prompt(),userId);

        return ResponseEntity.ok(new AiChatResponse(reply));
    }
}
