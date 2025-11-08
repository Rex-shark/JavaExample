package com.rex.linebotgame1.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ApiGameResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiGameResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiGameResponse<T> ok(T data) {
        return new ApiGameResponse<>(true, "成功", data);
    }

    public static <T> ApiGameResponse<T> fail(String message) {
        return new ApiGameResponse<>(false, message, null);
    }
}
