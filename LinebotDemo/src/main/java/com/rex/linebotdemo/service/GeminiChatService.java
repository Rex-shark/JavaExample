package com.rex.linebotdemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rex.linebotdemo.rag.RagProperties;
import com.rex.linebotdemo.rag.RagService;
import com.rex.linebotdemo.response.GeminiResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GeminiChatService implements AiChatService {
    @Value("${ai.gemini.api-key}")
    private String apiKey;

    // 可用 application.properties 調整 system prompt
    @Value("${ai.gemini.system-prompt:你是一個TIST尾牙機器人、說話風格俏皮有趣。回答請使用繁體中文，台灣用語。}")
    private String systemPrompt;

    private String API_URL;

    private final WebClient webClient = WebClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    private final RagProperties ragProperties;
    private final RagService ragService;

    @Value("${ai.rag.fallback-message:我只回答尾牙相關問題}")
    private String ragFallbackMessage;

    public GeminiChatService(RagProperties ragProperties, RagService ragService) {
        this.ragProperties = ragProperties;
        this.ragService = ragService;
    }

    @PostConstruct
    public void init() {
        API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
    }

    @Override
    public String chat(String userMessage) throws JsonProcessingException {
        String context = "";
        if (ragProperties.isEnabled()) {
            context = ragService.buildContext(userMessage);
            // RAG 有開但沒命中：直接回覆制式文字，不呼叫 AI
            if (context == null || context.isBlank()) {
                return ragFallbackMessage;
            }
        }

        String finalPrompt = composePrompt(systemPrompt, context, userMessage);

        // 用 Map 組 JSON，並直接讓 WebClient 序列化，避免把 JSON 當字串再包一層引號
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", finalPrompt)
                        })
                }
        );

        String responseBody = webClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        GeminiResponse resp = mapper.readValue(responseBody, GeminiResponse.class);
        return resp.candidates[0].content.parts[0].text;
    }

    /**
     * 把 system prompt + (可選)RAG context + user message 組成最終 prompt。
     * 這裡用單一 text 最小改動；未來也可以改成 Gemini 的多段 message 結構。
     */
    static String composePrompt(String systemPrompt, String context, String userMessage) {
        String sp = systemPrompt == null ? "" : systemPrompt.trim();
        String um = userMessage == null ? "" : userMessage.trim();
        String ctx = context == null ? "" : context.trim();

        if (ctx.isBlank()) {
            return """
                   [SYSTEM]
                   %s

                   [USER]
                   %s
                   """.formatted(sp, um).trim();
        }

        return """
               [SYSTEM]
               %s

               [CONTEXT]
               下面是檢索到的參考資料。請優先根據它回答；若資料不足請直接說不知道，不要編造。
               ---
               %s
               ---

               [USER]
               %s
               """.formatted(sp, ctx, um).trim();
    }
}
