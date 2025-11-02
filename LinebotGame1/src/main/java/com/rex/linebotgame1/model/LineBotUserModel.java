package com.rex.linebotgame1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineBotUserModel {
    String name;
    String nickname;
    String lineUserId;
    String title;
    String message;
    String imageUrl;

}
