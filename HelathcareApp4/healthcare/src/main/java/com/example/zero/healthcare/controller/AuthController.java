package com.example.zero.healthcare.controller;

import com.example.zero.healthcare.auth.JwtTokenProvider;
import com.example.zero.healthcare.dto.GoogleLoginRequest;
import com.example.zero.healthcare.dto.UserResponse;
import com.example.zero.healthcare.dto.common.ApiResponse;
import com.example.zero.healthcare.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody GoogleLoginRequest request) {
        UserResponse user = userService.loginOrRegister(request);
        return ResponseEntity.ok(ApiResponse.ok(user, "로그인 성공"));
    }
}
