package com.rex.jpamysql.controller;

import com.rex.jpamysql.entity.UserBase;
import com.rex.jpamysql.request.UserRequest;
import com.rex.jpamysql.service.UserService;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/users")
public class UserController {

    @Resource
    private UserService userService;


    @PostMapping
    public ResponseEntity<UserBase> create(@RequestBody UserRequest user) {
        UserBase created = userService.create(user.toEntity() );

        // 回傳 201 與建立好的物件
        return ResponseEntity.created(URI.create("/users/" + created.getUuid())).body(created);

    }

    @GetMapping
    public ResponseEntity<List<UserBase>> list() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserBase> getByUuid(@PathVariable String uuid) {
        return userService.findByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<UserBase> update(@PathVariable String uuid, @RequestBody UserBase user) {
        try {
            UserBase updated = userService.update(uuid, user);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid) {
        try {
            userService.deleteByUuid(uuid);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
