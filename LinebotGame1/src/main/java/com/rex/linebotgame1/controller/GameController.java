package com.rex.linebotgame1.controller;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.rex.linebotgame1.model.ApiGameResponse;
import com.rex.linebotgame1.model.DrawGameModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {

    @GetMapping("/draw_game")
    @ResponseBody
    public ResponseEntity<ApiGameResponse<DrawGameModel>> getDrawGameTopic( @RequestParam("id") String id ) {
        String answer ;
        List<String> prompts;
        int level;

        switch (id) {
            case "1":
                answer = "馬";
                prompts = List.of("一個字", "動物", "四條腿");
                level = 1;
                break;
            case "2":
                answer = "赤兔馬";
                prompts = List.of("三個字", "動物", "紅色的", "三國時代");
                level = 2;
                break;
            case "3":
                answer = "馬鈴薯";
                prompts = List.of("三個字", "食物", "澱粉類", "可以炸");
                level = 2;
                break;
            case "4":
                answer = "馬賽克";
                prompts = List.of("三個字", "模糊", "一格一格的");
                level = 3;
                break;
            case "5":
                answer = "特洛伊木馬";
                prompts = List.of("五個字", "古希臘", "木頭做的");
                level = 4;
                break;
            case "6":
                answer = "旋轉木馬";
                prompts = List.of("四個字", "遊樂園", "會轉的");
                level = 2;
                break;
            case "7":
                answer = "瑪利歐";
                prompts = List.of("三個字", "紅帽子","任天堂","遊戲角色");
                level = 2;
                break;
            default:
                answer = "馬";
                prompts = List.of("一個字", "動物", "四條腿");
                level = 1;
                break;
        }

        // 建立測試用的遊戲資料
        DrawGameModel model = DrawGameModel.builder()
                .id(id)
                .answer(answer)
                .prompts(prompts)
                .level(level)
                .build();


        ApiGameResponse<DrawGameModel> response = ApiGameResponse.ok(model);
        System.out.println("response = " + response);
        // 回傳 HTTP 200 OK 狀態
        return ResponseEntity.ok(response);
    }
}
