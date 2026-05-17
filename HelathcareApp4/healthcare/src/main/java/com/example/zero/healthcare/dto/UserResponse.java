package com.example.zero.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    @Schema(description = "사용자 ID", example = "1")
    private final Long userId;

    @Schema(description = "JWT 액세스 토큰")
    private final String accessToken;

}
