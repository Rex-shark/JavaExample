package com.rex.linebotgame1.controller;


import com.rex.linebotgame1.enums.GameMessageType;
import com.rex.linebotgame1.handler.GameWebSocketHandler;
import com.rex.linebotgame1.model.GameMessageModel;
import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import com.rex.linebotgame1.model.SocketMessageResponse;
import com.rex.linebotgame1.service.LineBotUserService;
import jakarta.annotation.Resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;
import java.util.Optional;



@RestController
@RequestMapping("/linebot-message-demo")
public class LinebotMessageDemoController {

    @Resource
    GameWebSocketHandler wsHandler;

    @Resource
    LineBotUserService lineBotUserService;

    /**
     * 利用API模擬linebot發送遊戲指令
     * @param body
     * @return
     */
    @PostMapping("/game-websocket")
    @ResponseBody
    public ResponseEntity<String> gameWebsocket(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String text = body.get("text");
        String type = body.get("type");
        String roomId = body.get("roomId");
        String groupId = body.get("groupId");

        //System.out.println("body = " + body);

        MessageContext ctx = MessageContext.builder()
                .replyToken("")
                .userId(userId)
                .groupId(groupId)
                .text(text)
                .build();

        Optional<LineBotUserModel> userOpt = lineBotUserService.getUserByLineUserId(ctx);
        if (userOpt.isPresent()) {
            LineBotUserModel user = userOpt.get();
            System.out.println("找到使用者：" + user.getName() );
            //lineBotApiService.getLintBotUser(ctx); // 呼叫 API 取得最新使用者資料

            GameMessageModel gameMessage = GameMessageModel.builder()
                    .type(GameMessageType.from(type))
                    .text(text)
                    .status("1")
                    .build();

            SocketMessageResponse socketResponse = SocketMessageResponse.builder()
                    .success(true)
                    .roomId(roomId)
                    .user(user)
                    .game(gameMessage)
                    .message("\uD83D\uDE0A")
                    .build();

            wsHandler.sendToRoom(socketResponse);
            return ResponseEntity.ok(user.getNickname()+" 發送遊戲指令：" + text);
        } else {
            System.out.println("找不到這個 userId");
            return ResponseEntity.ok("您尚未註冊！" );
        }

    }


}
