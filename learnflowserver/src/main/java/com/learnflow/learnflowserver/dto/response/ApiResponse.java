package com.learnflow.learnflowserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data);
    }

    public static ApiResponse<String> error(String message) {
        return new ApiResponse<>("error", message);
    }
}