package com.rex.linebotgame1.handler;

import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.TextMessage;
import com.rex.linebotgame1.enums.GameMessageType;
import com.rex.linebotgame1.model.GameMessageModel;
import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import com.rex.linebotgame1.model.SocketMessageResponse;
import com.rex.linebotgame1.service.LineBotUserService;
import com.rex.linebotgame1.service.TistUserService;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(1)
public class StarKeyCommandHandler implements LineBotMessageHandler{


    @Resource
    LineBotUserService lineBotUserService;

    private final GameWebSocketHandler wsHandler;

    public StarKeyCommandHandler(GameWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    @Override
    public boolean canHandle(MessageContext ctx) {
        String t = ctx.getText();
        return t != null && t.startsWith("*");
    }

    @Override
    public Message handle(MessageContext ctx) {
        String t = ctx.getText().trim();
        String roomId = "default";
        if (t.startsWith("*")) {
            t = t.substring(1).trim();
            if (t.length() > 20) {
                return new TextMessage("聊天訊息長度不可超過20個字❌");
            }
            Optional<LineBotUserModel> userOpt = lineBotUserService.getUserByLineUserId(ctx);
            if (userOpt.isEmpty()) {
                System.out.println("找不到這個 userId");
                return new TextMessage("您尚未註冊❗" );
            }
            LineBotUserModel user = userOpt.get();
            //lineBotApiService.getLintBotUser(ctx); // 呼叫 API 取得最新使用者資料
            GameMessageModel gameMessage = new GameMessageModel();
            gameMessage.setText(t);
            gameMessage.setType(GameMessageType.MESSAGE);
            gameMessage.setStatus("1");

            SocketMessageResponse socketResponse = new SocketMessageResponse();
            socketResponse.setSuccess(true);
            socketResponse.setRoomId(roomId);
            socketResponse.setUser(user);
            socketResponse.setGame(gameMessage);
            socketResponse.setMessage("\uD83D\uDE0A");

            wsHandler.sendToRoom(socketResponse);
            System.out.println("收到註冊指令！");
        }
        return new TextMessage("收到指令：" + t);
    }
}
