package com.rex.linebotgame1.dispatcher;

import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.TextMessage;
import com.rex.linebotgame1.handler.LineBotMessageHandler;
import com.rex.linebotgame1.model.MessageContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageDispatcher {
    private final List<LineBotMessageHandler> handlers;

    public MessageDispatcher(List<LineBotMessageHandler> handlers) {
        this.handlers = handlers;
    }

    public Message dispatch(MessageContext ctx) {
        for (LineBotMessageHandler h : handlers) {
            if (h.canHandle(ctx)) {
                return h.handle(ctx);
            }
        }
         return null;// 無對應指令則不回覆
    }
}