package com.rex.websocketdemo.controller;

import com.rex.websocketdemo.handler.GameWebSocketHandler;
import com.rex.websocketdemo.model.SocketMessageResponse;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/move")
public class MoveController {

    private final GameWebSocketHandler wsHandler;

    public MoveController(GameWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }


    // HTTP 範例：
    // 全域廣播：GET /move/move?text=Hello
    // 指定房間：GET /move/move?text=Hello&id=roomA
    @GetMapping("/move")
    public ResponseEntity<String> move(@RequestParam("text") String text,
                                       @RequestParam(value = "id", required = false) String roomId) {
        if (roomId == null || roomId.isBlank()) {
            wsHandler.broadcastText(text);
        } else {
            wsHandler.sendToRoom(roomId, text);
        }
        return ResponseEntity.ok("ok");
    }


    @GetMapping("/move2")
    public ResponseEntity<String> move2(@RequestParam(value = "id", required = false) String roomId,
                                        @RequestParam("name") String name,
                                        @RequestParam("dir") String dir,
                                        @RequestParam("title") String title)  {
        if (roomId == null || roomId.isBlank()) {
            wsHandler.broadcastText(dir);
        } else {
            wsHandler.sendToRoom(new SocketMessageResponse(title, name,dir,"xxx"),roomId);
        }
        return ResponseEntity.ok("ok");
    }
}

