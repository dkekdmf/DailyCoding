package com.example.zero.healthcare.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "항상 true", example = "true")
    private final boolean success = true;

    @Schema(description = "응답 데이터")
    private final T data;

    @Schema(description = "응답 메시지 (선택)")
    private final String message;

    private ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(data, message);
    }
}
