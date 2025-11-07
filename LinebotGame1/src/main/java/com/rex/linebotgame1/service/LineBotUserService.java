package com.rex.linebotgame1.service;

import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class LineBotUserService {

    //去資料庫抓TIST使用者(還沒做好)
    public Optional<LineBotUserModel> getUserByLineUserId(MessageContext ctx ) {
        // 模擬從資料庫或其他來源取得使用者資料
        return findUser(ctx);
    }

    // 模擬查找使用者的方法
    private Optional<LineBotUserModel> findUser(MessageContext ctx) {

        String id = ctx.getUserId();
        //假的user list
        ArrayList<LineBotUserModel> lineBotUserModels = new ArrayList<>();
        LineBotUserModel user1 = LineBotUserModel.builder()
                .lineUserId("Ubcfda06fb16affb4b6297038a0bddca5")
                .name("Rex")
                .nickname("Rex")
                .unitName("智慧軟體處")
                .imageUrl("https://sprofile.line-scdn.net/0hGvfrdyx7GFlLCwlj3E9mZztbGzNoekFLMjoHbCoJEmombl5bYGtRPikIE2ElaV9cZToAbX4OFG1peD5sDj8jOjpcA24FeD4GATBTSzh_QxoTW1pyZggsZgRULREWXBkKAB4cShdPOWxxQC1IOhIKeAkKDRMzZCRdFVx0D045dtokCW8MZmxeOn8PRW_-")
                .build();

        LineBotUserModel user2 = LineBotUserModel.builder()
                .lineUserId("U7f33bfbfb701596a5499c87c6539e60e")
                .name("Yu Wei")
                .nickname("Yu Wei")
                .unitName("產業發展處")
                .imageUrl("https://sprofile.line-scdn.net/0hUEHfoSsiCmplHxumN9Z0FRVPCQBGblN4TXwWXlMbBg5RLxk0QXFFBQMZUghQKRk1HikWWFZKBg5pDH0Me0n2XmIvV1tZJkg9TXpDhA")
                .build();

        LineBotUserModel user3 = LineBotUserModel.builder()
                .lineUserId("U0474089a130f97a992455b2cbbb5ac5c")
                .name("連軒妤")
                .nickname("軒妤")
                .unitName("創新應用處")
                .imageUrl("https://sprofile.line-scdn.net/0hsB3h5o5oLG0eETz7hO1SEm5BLwc9YHV_MnVhXy4Rd1x3JWM7ZnZkCHkTd1kjIG09OnIxWykXdlUSAlsLAEfQWRkhcVwiKG46NnRlgw")
                .build();

        //連軒妤,T450,2500662,創新應用處

        lineBotUserModels.add(user1);
        lineBotUserModels.add(user2);
        lineBotUserModels.add(user3);
        return lineBotUserModels.stream()
                .filter(user -> user.getLineUserId().equals(id))
                .findFirst();
    }

}
