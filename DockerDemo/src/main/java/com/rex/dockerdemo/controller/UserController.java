package com.rex.dockerdemo.controller;


import com.rex.dockerdemo.entity.UserBase;
import com.rex.dockerdemo.request.UserRequest;
import com.rex.dockerdemo.service.UserService;
import com.rex.dockerdemo.response.ApiResponse;
import jakarta.annotation.Resource;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:3000"})
@SuppressWarnings("unused")
@RestController
@RequestMapping("/users")
public class UserController {

    @Resource
    private UserService userService;


    @PostMapping
    public ResponseEntity<ApiResponse<UserBase>> create(@RequestBody UserRequest user) {
        UserBase created = userService.create(user.toEntity() );

        // 回傳 201 與建立好的物件
        return ResponseEntity.created(URI.create("/users/" + created.getUuid()))
                .body(ApiResponse.created(created));

    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserBase>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAll()));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserBase>> getByUuid(@PathVariable String uuid) {
        return userService.findByUuid(uuid)
                .map(u -> ResponseEntity.ok(ApiResponse.ok(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("user not found")));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserBase>> update(@PathVariable String uuid, @RequestBody UserBase user) {
        try {
            UserBase updated = userService.update(uuid, user);
            return ResponseEntity.ok(ApiResponse.ok(updated));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("user not found"));
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String uuid) {
        try {
            userService.deleteByUuid(uuid);
            // 回傳成功但無內容的 data
            return ResponseEntity.ok(new ApiResponse<>(true, null, "deleted", null, 200));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("user not found"));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(ex.getMessage()));
    }
}
