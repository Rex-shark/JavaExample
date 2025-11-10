package com.rex.linebotgame1.controller;

import com.rex.linebotgame1.model.ApiGameResponse;
import com.rex.linebotgame1.model.DrawGameModel;
import com.rex.linebotgame1.model.WhoAmIGameModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/game")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

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

    @GetMapping("/who_am_i_game")
    @ResponseBody
    public ResponseEntity<ApiGameResponse<WhoAmIGameModel>> getWhoAmIGameTopic(@RequestParam("id") String id ) {


        String displayName;
        List<String> answers;
        List<String> prompts;
        int level;

        String questionImageUrl;
        String promptImageUrl;
        String answerImageUrl;

        switch (id) {
            case "1":
                displayName = "皮卡丘";
                answers  = List.of("皮卡丘", "皮神");
                prompts = List.of("黃色的", "電屬性", "老鼠");
                level = 1;
                // 由 resources/b64img 讀取，檔名依 id 組成（例如: 0_question.txt）
                questionImageUrl = "https://lh3.google.com/u/0/d/1NfoBYs17fmkT_FFvSbTX-RPb873BV9EW=w1920-h945-iv1?auditContext=thumbnail&auditContext=prefetch";
                promptImageUrl = "https://lh3.google.com/u/0/d/1TQeGygHIdnEaxb13UCcKjjDwW71abQE2=w1920-h877-iv1?auditContext=thumbnail&auditContext=prefetch";
                answerImageUrl = "https://lh3.google.com/u/0/d/1c6zxkxyDk4nHiG2edCigiDjjnHbopZdW=w1920-h945-iv1?auditContext=thumbnail&auditContext=prefetch";
                break;
            default:
                displayName = "皮卡丘";
                answers  = List.of("皮卡丘", "皮神");
                prompts = List.of("黃色的", "電屬性", "老鼠");
                level = 1;
                // 由 resources/b64img 讀取，檔名依 id 組成（例如: 0_question.txt）
                questionImageUrl= "";
                promptImageUrl ="";
                answerImageUrl = "";
                break;
        }


        // 建立測試用的遊戲資料
        WhoAmIGameModel model = WhoAmIGameModel.builder()
                .id(id)
                .displayName(displayName)
                .questionImageUrl(questionImageUrl)
                .promptImageUrl(promptImageUrl)
                .answerImageUrl(answerImageUrl)
                .prompts(prompts)
                .answers(answers)
                .level(level)
                .build();

        ApiGameResponse<WhoAmIGameModel> response = ApiGameResponse.ok(model);
        System.out.println("response = " + response);
        // 回傳 HTTP 200 OK 狀態
        return ResponseEntity.ok(response);
    }


}
