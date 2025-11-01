package com.rex.linebotdemo.controller;

import com.linecorp.bot.client.base.Result;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.PushMessageRequest;
import com.linecorp.bot.messaging.model.PushMessageResponse;
import com.linecorp.bot.messaging.model.TextMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/test")
public class TestController {

    @Value("${line.bot.channel-token}")
    private String channelToken;

    @PutMapping("/sendMessage/{id}")
    @ResponseBody
    public ResponseEntity<String> sendMessage(@PathVariable String id) {

        String userId = id ;

        //---
        // 建立 client（用您反編譯看到的 builder 方法）
        MessagingApiClient apiClient = MessagingApiClient.builder(channelToken).build();

        // 建立 TextMessage（constructor 依版本可能不同，請以您反編譯看到的為準）
        // 可能範例 1: new TextMessage("text", "內容")
        // 可能範例 2: new TextMessage("內容")
        Message textMsg = new TextMessage("哈囉主人，這是來自機器人的通知喵 ~～");

        // 建立 PushMessageRequest（用 inner Builder）
        PushMessageRequest request = new PushMessageRequest.Builder(
                userId,
                List.of(textMsg)
        ).notificationDisabled(false)
                .build();

        // 必須提供一個 UUID 作為 X-Line-Retry-Key header
        UUID retryKey = UUID.randomUUID();

        // 呼叫 pushMessage -> 會回傳 CompletableFuture<Result<PushMessageResponse>>
        CompletableFuture<Result<PushMessageResponse>> future = apiClient.pushMessage(retryKey, request);

        // 同步等待（示範）並處理結果/錯誤

        try {
            Result<PushMessageResponse> result = future.get(); // 可能拋出 InterruptedException / ExecutionException
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.out.println("已發送訊息給 userId=" + userId);
        //--
        return ResponseEntity.ok("Test endpoint reached successfully");
    }

    @GetMapping("/findUserId")
    @ResponseBody
    public ResponseEntity<String> findUserId() {

        String userId = "XXX"; // 替換為實際的 userId
        // 建立 client（用您反編譯看到的 builder 方法）
        MessagingApiClient apiClient = MessagingApiClient.builder(channelToken).build();

        //--
        return ResponseEntity.ok("findUserId successfully");
    }

}
