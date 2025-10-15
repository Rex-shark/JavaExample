package com.rex.redisdemo.controller;

import com.rex.redisdemo.request.RedisRequest;
import com.rex.redisdemo.service.RedisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/redis")
public class RedisController {

    private final RedisService redisService;

    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }


    @PostMapping("/get")
    public ResponseEntity<String> get(@RequestBody RedisRequest request) {
        return redisService.get(request.getKey())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/all")
    public ResponseEntity<Map<String, String>> getAll() {
        return ResponseEntity.ok(redisService.getAll());
    }

    @PostMapping("/set")
    public ResponseEntity<Void> set(@RequestBody RedisRequest request) {
        redisService.set(request.getKey(), request.getValue());
        return ResponseEntity.ok().build();
    }



    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody RedisRequest request) {
        boolean removed = redisService.delete(request.getKey());
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestBody RedisRequest request) {
        return ResponseEntity.ok(redisService.exists(request.getKey()));
    }
}
