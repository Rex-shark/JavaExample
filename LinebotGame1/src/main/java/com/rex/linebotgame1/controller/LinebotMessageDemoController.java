package com.rex.linebotgame1.controller;

import com.linecorp.bot.client.base.Result;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.PushMessageRequest;
import com.linecorp.bot.messaging.model.PushMessageResponse;
import com.linecorp.bot.messaging.model.TextMessage;
import com.rex.linebotgame1.enums.GameMessageType;
import com.rex.linebotgame1.handler.GameWebSocketHandler;
import com.rex.linebotgame1.model.GameMessageModel;
import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import com.rex.linebotgame1.model.SocketMessageResponse;
import com.rex.linebotgame1.service.LineBotUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/linebot-message-demo")
public class LinebotMessageDemoController {

    @Resource
    private  GameWebSocketHandler wsHandler;

    @Resource
    LineBotUserService lineBotUserService;

    @PostMapping("/game-websocket")
    @ResponseBody
    public ResponseEntity<String> gameWebsocket(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String text = body.get("text");
        String type = body.get("type");
        String roomId = body.get("roomId");
        String groupId = body.get("groupId");

        System.out.println("body = " + body);


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
            GameMessageModel gameMessage = new GameMessageModel();
            gameMessage.setText(text);
            gameMessage.setType(GameMessageType.from(type));
            gameMessage.setStatus("1");

            SocketMessageResponse socketResponse = new SocketMessageResponse();
            socketResponse.setSuccess(true);
            socketResponse.setRoomId(roomId);
            socketResponse.setUser(user);
            socketResponse.setGame(gameMessage);
            socketResponse.setMessage("\uD83D\uDE0A");

            wsHandler.sendToRoom(socketResponse);
            return ResponseEntity.ok(user.getNickname()+" 發送遊戲指令：" + text);
        } else {
            System.out.println("找不到這個 userId");
            return ResponseEntity.ok("您尚未註冊！" );
        }

    }


}
