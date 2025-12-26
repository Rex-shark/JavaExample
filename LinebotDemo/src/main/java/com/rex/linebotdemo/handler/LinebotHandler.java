package com.rex.linebotdemo.handler;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.linecorp.bot.client.base.Result;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;

import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.*;

import com.rex.linebotdemo.service.GeminiChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;


@Slf4j
@LineMessageHandler
public class LinebotHandler {

    @Value("${line.bot.channel-secret}")
    private String channelSecret;

    @Value("${line.bot.channel-token}")
    private String channelToken;

    @Resource
    GeminiChatService geminiChatService;

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent event) throws JsonProcessingException {

        System.out.println("handleTextMessageEvent");
        System.out.println("event = " + event);
        MessageContent content = event.message();
        String userText = "";

        String userId = null;
        Source source = event.source();
        AtomicReference<String> originalMessageText = new AtomicReference<>();


        if (source instanceof GroupSource groupSource) {
            System.out.println("🐾 這是群組訊息喵！");
            System.out.println("群組 ID：" + groupSource.groupId());

            if (content instanceof TextMessageContent textMessage) {
                System.out.println("🗨️ 使用者傳的文字：" + textMessage.text());

            } else if (content instanceof ImageMessageContent imageMessage) {
                System.out.println("🖼️ 使用者傳了一張圖片喵！");
                System.out.println("圖片 ID：" + imageMessage.id());
            } else if (content instanceof StickerMessageContent sticker) {
                System.out.println("💬 使用者傳了貼圖！");
                System.out.println("貼圖 ID：" + sticker.stickerId());
            } else {
                System.out.println("📦 其他訊息類型：" + content.getClass().getSimpleName());
            }

        } else if (source instanceof RoomSource roomSource) {
            System.out.println("🐾 這是多人聊天室訊息喵！");
            System.out.println("聊天室 ID：" + roomSource.roomId());
        } else if (source instanceof UserSource userSource) {
            System.out.println("🐾 這是個人訊息喵！");
            System.out.println("使用者 ID：" + userSource.userId());
            if (content instanceof TextMessageContent textMessage) {
                System.out.println("🗨️ 使用者傳的文字：" + textMessage.text());
                userText = textMessage.text();
            }
        } else {
            System.out.println("😿 無法辨識來源類型喵！");
        }

        userId = source.userId();
        System.out.println("userId = " + userId);

//        MessagingApiClient client = MessagingApiClient.builder(channelToken)
//                .apiEndPoint(URI.create("https://api.line.me/")) // 可省略，預設就是這個
//                .build();


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

//        if (originalMessageText.get() == null || originalMessageText.get().isEmpty()) {
//            return this.getRandomResponse(); // 如果沒有回應內容，則不發送任何消息
//        }

        //AI
        if(userText.isEmpty()){
            //隨機亂回
            return this.getRandomResponse();
        }
        System.out.println( "AI 模組整合區域");
        System.out.println("userText= " + userText);
        //userText 開頭是/ai 則進入AI回覆
        if (userText.toLowerCase(java.util.Locale.ROOT).startsWith("/ai")) {
            userText = userText.substring(3).trim(); // 移除開頭的 /AI 並去除多餘空白
            String aiResponse = geminiChatService.chat(userText );
            return new TextMessage(aiResponse);
        }


        //隨機亂回
        return this.getRandomResponse();



        //新模組 單人
        //event = MessageEvent[source=UserSource[userId=U4572b96a8d20e5523f085c51e2205507]
        // , timestamp=1751795411970, mode=ACTIVE, webhookEventId=01JZFJ9BFXFP385FQBFWQV26FY
        // , deliveryContext=DeliveryContext[isRedelivery=false]
        // , replyToken=444941425b034eb0b3ce149deeb2c1a1
        // , message=TextMessageContent[id=568687405549486235, text=0, emojis=null, mention=null, quoteToken=zVKIS4cN28qUPBYvl-YlQKxdyWoqJfhQiJXt2yQC0R3KYihdRfQX97xgF-qC_MPFI4wMc8R-nrn5KNnLV_bvrhFfbLZFNgR-muCxD4yIbUO2Zhg13zP2Ekg2VvuqPBID5kw6To9WhPTtr1pUej9vEw, quotedMessageId=null]]
        //新模組 群組
        //a event = MessageEvent[source=GroupSource[groupId=C1262e413e56d7405a9f86aa0da8280fe, userId=U4572b96a8d20e5523f085c51e2205507], timestamp=1751808478086, mode=ACTIVE, webhookEventId=01JZFYR31RPMJ14TGNQG799YH8, deliveryContext=DeliveryContext[isRedelivery=false], replyToken=32b676b1f51145078089f0c733530da9, message=TextMessageContent[id=568709326844199520, text=., emojis=null, mention=null, quoteToken=moNxkWo4VOResAgc-N5xvH2VSJgL-97YzLV5zNnPWq29cSOdFGiPNaOHLj4EgZCXgyQ4tzPy2W5gkdzcxU64qLN6j7jY-nGP_bKGmN5Kv1mvS3NM7UQCe5O-QsLx1balPVDH293JSxMXMJ8DoMyJkg, quotedMessageId=null]]


        //單人
        //內容記錄TextMessageContent(id=568680190742626666, text=0, emojis=null, mention=null)
        //內容記錄MessageEvent(replyToken=9f2066c6d919432a843e715f5887dafc
        // , source=UserSource(userId=U4572b96a8d20e5523f085c51e2205507)
        // , message=TextMessageContent(id=568680190742626666, text=0, emojis=null, mention=null)
        // , timestamp=2025-07-06T08:38:31.614Z, mode=ACTIVE, webhookEventId=01JZFE63ZJYG87AFBC3TE6M981
        // , deliveryContext=DeliveryContext(isRedelivery=false))

        //群組
        //內容記錄MessageEvent(replyToken=0cd6d7cadce343c08298c8df81723755
        //內容記錄messageContent = TextMessageContent(id=568680530112675842, text=0, emojis=null, mention=null)
        // , source=GroupSource(groupId=C1262e413e56d7405a9f86aa0da8280fe
        // , userId=U4572b96a8d20e5523f085c51e2205507)
        // , message=TextMessageContent(id=568680530112675842, text=0, emojis=null, mention=null)
        // , timestamp=2025-07-06T08:41:53.929Z, mode=ACTIVE, webhookEventId=01JZFEC9G2Q0MKTQ3A1R3X8SAC
        // , deliveryContext=DeliveryContext(isRedelivery=false))


    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("handleDefaultMessageEvent: " + event);
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
