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
        // 建立測試用的遊戲資料
        DrawGameModel model = DrawGameModel.builder()
                .Id(id)
                .answer("貓")
                .prompts(List.of("一個字", "動物"))
                .level(2)
                .build();



        // 包成標準 API 回傳格式
        ApiGameResponse<DrawGameModel> response = ApiGameResponse.ok(model);

        // 回傳 HTTP 200 OK 狀態
        return ResponseEntity.ok(response);
    }
}
