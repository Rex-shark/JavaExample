package org.example.aiopenaidemo.service;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class OpenAiChatService implements AiChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Random random = new Random();

    @Resource
    private ChatMemory chatMemory;

    public OpenAiChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }


    @Override
    public String chat(String systemPrompt, String userPrompt, String conversationId) {

        System.out.println("userPrompt = " + userPrompt);
        // 1. 【手動檢索】使用 Builder 模式建立搜尋請求
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userPrompt)           // 設定查詢字串
                        .topK(5)                     // 設定筆數 (注意方法名是 topK，不是 withTopK)
                        .similarityThreshold(0.1)    // 設定相似度門檻
                        .build()
        );

        // 2. 【事前判斷】如果沒命中，直接回傳，不呼叫 AI
        if (docs.isEmpty()) {
            System.out.println("RAG 未命中：資料庫找不到相關內容。");

            // 策略 A: 直接回傳固定訊息 (省錢、極快)
            // return "抱歉，關於這個問題，我的知識庫裡沒有相關資料。";

            // 策略 B: 繼續呼叫 AI，但標記說沒資料 (使用 AI 的閒聊能力)
            // (這裡演示繼續往下走的情況)
        } else {
            System.out.println("RAG 命中 " + docs.size() + " 筆資料！");
            int rank = 1;
            // 這裡您可以查看 docs 內容
            for (Document doc : docs) {
                System.out.println("\n=== 第 " + rank + " 名 ===");

                // 1. 取得分數 (不同資料庫 Key 可能不同，SimpleVectorStore 通常是 'distance')
                // 注意：SimpleVectorStore 的 'distance' 其實是 1 - CosineSimilarity
                // 所以分數越低，代表距離越近 (越相似)；或者是反過來，視版本而定。
                // 但 Spring AI 的 SearchRequest 使用 similarityThreshold 是 0.0~1.0 (越高越像)

                // 我們先嘗試印出所有 metadata 來看 Key 叫什麼名字
                System.out.println("【Metadata 內容】: " + doc.getMetadata());

                // 嘗試取得分數 (假設 Key 是 distance)
                Object scoreObj = doc.getMetadata().get("distance");

                if (scoreObj != null) {
                    // 轉成 Double 顯示
                    Double score = Double.valueOf(scoreObj.toString());
                    // 如果是距離 (Distance)，通常越小越好；如果是相似度 (Similarity)，越大越好
                    // 在 Spring AI SimpleVectorStore 中，這裡顯示的通常是相似度 (例如 0.85)
                    System.out.printf("🎯 相似度分數: %.4f (越高代表越精準)%n", 1-score);
                } else {
                    System.out.println("⚠️ 無法取得相似度分數 (請檢查 Metadata Key)");
                }

                System.out.println("📄 內容摘要: " + doc.getText().substring(0, Math.min(20000, doc.getText().length())) + "...");
                System.out.println("📂 來源資訊: " + doc.getMetadata().get("filename")); // 或是您設定的 source

                rank++;
            }
        }

//        if(true){
//            return "不發送API";
//        }

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

}
