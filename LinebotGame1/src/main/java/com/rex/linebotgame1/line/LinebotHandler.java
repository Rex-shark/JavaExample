package com.rex.linebotgame1.line;


import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.messaging.model.UserProfileResponse;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.*;
import com.rex.linebotgame1.dispatcher.MessageDispatcher;
import com.rex.linebotgame1.model.MessageContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
@LineMessageHandler
public class LinebotHandler {

    @Value("${line.bot.channel-secret}")
    private String channelSecret;

    @Value("${line.bot.channel-token}")
    private String channelToken;

    @Resource
    private MessageDispatcher dispatcher;

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent event) {

        MessageContent content = event.message();
        if (!(content instanceof TextMessageContent textMessage)) {
            return null;// 僅處理文字訊息
        }


        String userId = null;
        String groupId = null;
        Source source = event.source();
        if (source instanceof UserSource us) {
            userId = us.userId();
        } else if (source instanceof GroupSource gs) {
            groupId = gs.groupId();
            userId = gs.userId(); // 可能為 null（取決於使用者是否同意）
        } else if (source instanceof RoomSource rs) {
            userId = rs.userId();
        }
        System.out.println("userId = " + userId);
        MessageContext ctx = MessageContext.builder()
                .replyToken(event.replyToken())
                .userId(userId)
                .groupId(groupId)
                .text(textMessage.text())
                .build();

        // 交給責任鏈分派，無命中則不回覆
        return dispatcher.dispatch(ctx);


    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println(" 看到這個訊息請聯絡Rex，handleDefaultMessageEvent: " + event);
    }

    //臨時性設計 隨機回應文字
    private Message getRandomResponse() {
        //設計一個隨機機率1/100，才會隨機回答
        //如果隨機數小於1/100，則回應隨機文字
        if (Math.random() > 0.01) {
            return null; // 99% 機率不回應隨機文字
        }

        String[] responses = {
            "嗚啦呀哈！",
            "呀哈嗚拉！",
            "嗚拉呀哈嗚拉！",
            "嗚啦！",
            "呀哈！",
            "蛤！",
            "哼！",
            "噗魯魯魯魯！",
            "嗚啦呀哈！呀哈嗚拉",
            "噗哩！",

        };
        int randomIndex = (int) (Math.random() * responses.length);
        return new TextMessage(responses[randomIndex]);
    }
}

//根據id取得使用者資料範例程式碼
//        userId = source.userId();
//        System.out.println("userId = " + userId);
//
//        MessagingApiClient client = MessagingApiClient.builder(channelToken)
//                .apiEndPoint(URI.create("https://api.line.me/")) // 可省略，預設就是這個
//                .build();
//
//
//
//        // 呼叫 getProfile 取得使用者資料
//        client.getProfile(userId)
//                .whenComplete((profile, throwable) -> {
//                    if (throwable != null) {
//                        System.err.println("取得使用者資料時發生錯誤：" + throwable.getMessage());
//                        return;
//                    }
//
//                    if (profile != null) {
//                        UserProfileResponse userProfile = profile.body();
//                        originalMessageText.set(userProfile.displayName());
//
//                        System.out.println("profile = " + profile);
//                        System.out.println("✅ 使用者名稱：" + userProfile.displayName());
//                        System.out.println("🆔 User ID：" + userProfile.userId());
//                        System.out.println("🖼️ 大頭貼連結：" + userProfile.pictureUrl());
//                        System.out.println("✏️ 狀態訊息：" + userProfile.statusMessage());
//                    } else {
//                        System.out.println("查無使用者資料喵～");
//                    }
//                })
//                .join(); // 等待執行完成