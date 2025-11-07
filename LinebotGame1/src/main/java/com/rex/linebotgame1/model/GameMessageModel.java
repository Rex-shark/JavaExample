package com.rex.linebotgame1.model;

import com.rex.linebotgame1.enums.GameMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMessageModel {
    GameMessageType type;
    String text;
    String status;//保留使用，尚未定義

}
