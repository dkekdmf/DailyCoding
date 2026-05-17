package com.example.zero.healthcare.exception.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ErrorResponse {
    @Schema(description = "항상 false", example = "false")
    private final boolean success = false;

    @Schema(description = "에러 코드", example = "JOURNAL_NOT_FOUND")
    private final String code;

    @Schema(description = "에러 메시지", example = "해당 일지를 찾을 수 없습니다.")
    private final String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
