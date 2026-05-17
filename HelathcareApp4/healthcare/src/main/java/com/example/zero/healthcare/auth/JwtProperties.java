package com.example.zero.healthcare.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret = "ttamtam-default-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private long expirationDays = 30;
}
