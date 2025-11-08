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


    /**
     * 透過Line Messaging API取得使用者資料
     * @param ctx
     * @return
     */
    public Optional<LineBotUserModel> getLintBotUser(MessageContext ctx) {
        String userId = ctx.getUserId();
        System.out.println("API userId = " + userId);

        MessagingApiClient client = MessagingApiClient.builder(channelToken)
                .apiEndPoint(URI.create("https://api.line.me/"))
                .build();

        try {
            var profileResponse = client.getProfile(userId).join();
            if (profileResponse != null && profileResponse.body() != null) {
                UserProfileResponse userProfile = profileResponse.body();
                LineBotUserModel model = new LineBotUserModel();
                model.setImageUrl(String.valueOf(userProfile.pictureUrl()));
                model.setNickname(userProfile.displayName());
                model.setLineUserId(userProfile.userId());
                model.setMessage(userProfile.statusMessage());
//                System.out.println("String.valueOf(userProfile.pictureUrl()) = " + String.valueOf(userProfile.pictureUrl()));
//                System.out.println("userProfile.displayName() = " + userProfile.displayName());
//                System.out.println("userProfile.userId() = " + userProfile.userId());
//                System.out.println("userProfile.statusMessage() = " + userProfile.statusMessage());
                return Optional.of(model);
            }
        } catch (Exception e) {
            System.err.println("取得使用者資料錯誤：" + e.getMessage());
        }

        return Optional.empty();
    }

}