package com.rex.linebotgame1.service;

import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class LineBotUserService {

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
                .lineUserId("U")
                .name("Rex")
                .nickname("Rex")
                .title("智慧軟體處")
                .imageUrl("")
                .build();

        LineBotUserModel user2 = LineBotUserModel.builder()
                .lineUserId("U222222222222222222222222222222")
                .name("Amy")
                .nickname("Amy")
                .title("工程部")
                .build();

        lineBotUserModels.add(user1);
        lineBotUserModels.add(user2);
        return lineBotUserModels.stream()
                .filter(user -> user.getLineUserId().equals(id))
                .findFirst();
    }

}
