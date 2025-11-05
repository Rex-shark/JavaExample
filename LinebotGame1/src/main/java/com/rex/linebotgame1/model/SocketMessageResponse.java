package com.rex.linebotgame1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    String roomId;
    String message;
    LineBotUserModel user ;
    GameMessageModel game;

    // 宣告（放在類別內）
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 範例方法：物件轉 JSON
    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
