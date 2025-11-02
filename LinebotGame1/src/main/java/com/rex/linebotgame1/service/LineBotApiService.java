package com.rex.linebotgame1.service;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.UserProfileResponse;
import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class LineBotApiService {
    @Value("${line.bot.channel-token}")
    private String channelToken;


    public Optional<LineBotUserModel> getLintBotUser(MessageContext ctx) {
        String userId = ctx.getUserId();
        System.out.println("userId = " + userId);

        MessagingApiClient client = MessagingApiClient.builder(channelToken)
                .apiEndPoint(URI.create("https://api.line.me/")) // 可省略，預設就是這個
                .build();

        // 呼叫 getProfile 取得使用者資料
        client.getProfile(userId)
                .whenComplete((profile, throwable) -> {
                    if (throwable != null) {
                        System.err.println("取得使用者資料時發生錯誤：" + throwable.getMessage());
                        return;
                    }

                    if (profile != null) {
                        UserProfileResponse userProfile = profile.body();
                        System.out.println("profile = " + profile);
                        System.out.println("✅ 使用者名稱：" + userProfile.displayName());
                        System.out.println("🆔 User ID：" + userProfile.userId());
                        System.out.println("🖼️ 大頭貼連結：" + userProfile.pictureUrl());
                        System.out.println("✏️ 狀態訊息：" + userProfile.statusMessage());
                    } else {
                        System.out.println("查無使用者資料喵～");
                    }
                })
                .join(); // 等待執行完成


            return Optional.empty();
    }

}