package com.rex.linebotgame1.handler;

import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.TextMessage;
import com.rex.linebotgame1.model.MessageContext;
import com.rex.linebotgame1.service.TistUserService;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class DashCommandHandler  implements LineBotMessageHandler{


    @Resource
    TistUserService  tistUserService;

    @Override
    public boolean canHandle(MessageContext ctx) {
        String t = ctx.getText();
        return t != null && t.startsWith("-");
    }

    @Override
    public Message handle(MessageContext ctx) {
        String t = ctx.getText().trim();
        if ("-test".equalsIgnoreCase(t)) {
            return new TextMessage("test!");
        }
        if (t.startsWith("-註冊")) {
            System.out.println("收到註冊指令！");
            return new TextMessage(tistUserService.registerTistUser(ctx));
        }
        return new TextMessage("收到指令：" + t);
    }
}
