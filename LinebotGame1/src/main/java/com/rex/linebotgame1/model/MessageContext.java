package com.rex.linebotgame1.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageContext {
    private final String replyToken;
    private final String userId;
    private final String groupId;
    private final String text;
}