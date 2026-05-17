package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.auth.JwtTokenProvider;
import com.example.zero.healthcare.dto.GoogleLoginRequest;
import com.example.zero.healthcare.dto.UserResponse;
import com.example.zero.healthcare.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public UserResponse loginOrRegister(GoogleLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .map(existing -> {
          existing.setIdToken(request.getIdToken());
          return existing;
        })
        .orElseGet(() -> userRepository.save(
            new User(request.getEmail(), request.getIdToken(), request.getNickname())
        ));
    return new UserResponse(user.getId(), jwtTokenProvider.generateToken(user.getId()));
  }
}
