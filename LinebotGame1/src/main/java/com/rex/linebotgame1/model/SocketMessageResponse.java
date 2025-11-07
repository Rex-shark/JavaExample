package com.rex.linebotgame1.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rex.linebotgame1.enums.SocketMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SocketMessageResponse {

    boolean success;
    SocketMessageType type;
    String roomId;
    String message;
    LineBotUserModel user ;
    GameMessageModel game;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
