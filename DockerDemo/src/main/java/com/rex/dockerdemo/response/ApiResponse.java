package com.rex.dockerdemo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private List<String> errors;
    private long timestamp;
    private int status;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, T data, String message, List<String> errors, int status) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errors = errors;
        this.timestamp = Instant.now().toEpochMilli();
        this.status = status;
    }
    // factory helpers
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, 200);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data, "created", null, 201);
    }

    public static <T> ApiResponse<T> error(int status, String message, List<String> errors) {
        return new ApiResponse<>(false, null, message, errors, status);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(false, null, message, null, 400);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(false, null, message, null, 404);
    }
}