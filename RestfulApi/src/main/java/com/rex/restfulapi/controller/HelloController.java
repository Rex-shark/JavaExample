package com.rex.restfulapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping
    public ResponseEntity<String> getHello() {
        return ResponseEntity.ok("查詢成功");
    }

    @PostMapping
    public ResponseEntity<String> createHello() {
        return ResponseEntity.ok("新增成功");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateHello(@PathVariable String id) {
        return ResponseEntity.ok("更新成功：" + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHello(@PathVariable String id) {
        return ResponseEntity.ok("刪除成功：" + id);
    }
}

