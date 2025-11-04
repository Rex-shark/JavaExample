package com.rex.linebotgame1.model;

import com.rex.linebotgame1.entity.TistUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineBotUserModel {

    public LineBotUserModel(TistUser tistUser ){
        lineUserId = tistUser.getLineId();
        name = tistUser.getName();
        nickname = tistUser.getNickname();
        unitName = tistUser.getUnitName();
        title = tistUser.getTitle();
        imageUrl = tistUser.getImageUrl();
    }

    String lineUserId;
    String name;
    String nickname;
    String unitName;
    String title;
    String message;
    String imageUrl;

}
