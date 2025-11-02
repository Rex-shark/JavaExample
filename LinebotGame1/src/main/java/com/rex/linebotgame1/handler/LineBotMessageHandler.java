package com.rex.linebotgame1.handler;

import com.linecorp.bot.messaging.model.Message;
import com.rex.linebotgame1.model.MessageContext;

public interface LineBotMessageHandler {
    boolean canHandle(MessageContext ctx);
    Message handle(MessageContext ctx);
}
