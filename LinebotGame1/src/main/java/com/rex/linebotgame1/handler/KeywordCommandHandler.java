package com.rex.linebotgame1.handler;

import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.TextMessage;
import com.rex.linebotgame1.enums.GameMessageType;
import com.rex.linebotgame1.model.GameMessageModel;
import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import com.rex.linebotgame1.model.SocketMessageResponse;
import com.rex.linebotgame1.service.LineBotApiService;
import com.rex.linebotgame1.service.LineBotUserService;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(2) // 次優先：關鍵字
public class KeywordCommandHandler   implements LineBotMessageHandler{

    @Resource
    LineBotUserService lineBotUserService;

    @Resource
    LineBotApiService lineBotApiService;

    private final GameWebSocketHandler wsHandler;

    public KeywordCommandHandler(GameWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    @Override
    public boolean canHandle(MessageContext ctx) {
        String t = norm(ctx.getText());
        return "help".equals(t) || "幫助".equals(t) || t.equals("上")
                || t.equals("下")|| t.equals("左")|| t.equals("右")|| t.equals("加入")|| t.startsWith("猜");
    }

    @Override
    public Message handle(MessageContext ctx) {
        String t = norm(ctx.getText());

        if ("help".equals(t) || "幫助".equals(t)) {
            return new TextMessage("歡迎使用指令幫助！");
        }
        if(t.equals("上") || t.equals("下") || t.equals("左") || t.equals("右")){
            String roomId = "default";
            //將中文 上下左右轉為英文
            switch (t){
                case "上" -> t = "up";
                case "下" -> t = "down";
                case "左" -> t = "left";
                case "右" -> t = "right";
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
            gameMessage.setType(GameMessageType.MOVE);
            gameMessage.setStatus("1");

            SocketMessageResponse socketResponse = new SocketMessageResponse();
            socketResponse.setSuccess(true);
            socketResponse.setRoomId(roomId);
            socketResponse.setUser(user);
            socketResponse.setGame(gameMessage);
            socketResponse.setMessage("\uD83D\uDE0A");

            wsHandler.sendToRoom(socketResponse);
            return new TextMessage(user.getNickname()+" 移動：" + t);//TODO Rex null比較好
        }
        if ("加入".equals(t) ) {
            String roomId = "default";
            Optional<LineBotUserModel> userOpt = lineBotUserService.getUserByLineUserId(ctx);
            if (userOpt.isEmpty()) {
                return new TextMessage("您尚未註冊❗" );
            }
            LineBotUserModel user = userOpt.get();
            System.out.println("找到使用者：" + user.getName() );
            //lineBotApiService.getLintBotUser(ctx); // 呼叫 API 取得最新使用者資料
            GameMessageModel gameMessage = new GameMessageModel();
            gameMessage.setText(t);
            gameMessage.setType(GameMessageType.JOIN);
            gameMessage.setStatus("1");

            SocketMessageResponse socketResponse = new SocketMessageResponse();
            socketResponse.setSuccess(true);
            socketResponse.setRoomId(roomId);
            socketResponse.setUser(user);
            socketResponse.setGame(gameMessage);
            socketResponse.setMessage("\uD83D\uDE0A");

            wsHandler.sendToRoom(socketResponse);
            return new TextMessage( user.getName()+"已加入遊戲❗");
        }
        if (t.startsWith("猜")) {
            String roomId = "default";
            Optional<LineBotUserModel> userOpt = lineBotUserService.getUserByLineUserId(ctx);
            if (userOpt.isEmpty()) {
                return new TextMessage("您尚未註冊❗" );
            }
            LineBotUserModel user = userOpt.get();
            //lineBotApiService.getLintBotUser(ctx); // 呼叫 API 取得最新使用者資料
            GameMessageModel gameMessage = new GameMessageModel();
            //將t 移除一開始的猜跟空白
            t = t.substring(1).trim();
            System.out.println("t = " + t);//TODO 過濾特殊字元
            gameMessage.setText(t);
            gameMessage.setType(GameMessageType.GUESS);
            gameMessage.setStatus("1");

            SocketMessageResponse socketResponse = new SocketMessageResponse();
            socketResponse.setSuccess(true);
            socketResponse.setRoomId(roomId);
            socketResponse.setUser(user);
            socketResponse.setGame(gameMessage);
            socketResponse.setMessage("\uD83D\uDE0A");

            wsHandler.sendToRoom(socketResponse);
            return new TextMessage(user.getNickname()+" 猜：" + t);//TODO Rex null比較好
        }

        return null;
    }

    private String norm(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }
}
