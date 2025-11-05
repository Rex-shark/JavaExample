package com.rex.linebotgame1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMessageModel {
    String type;
    String text;
    String status;//保留使用，尚未定義

}
