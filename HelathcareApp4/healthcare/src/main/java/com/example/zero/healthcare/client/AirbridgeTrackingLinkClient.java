package com.example.zero.healthcare.client;

import com.example.zero.healthcare.config.AirbridgeProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AirbridgeTrackingLinkClient {

    private static final String BASE_URL = "https://api.airbridge.io";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AirbridgeProperties properties;

    public String createTrackingLink(String deeplinkUrl) {
        CreateRequest request = new CreateRequest("invite", deeplinkUrl);

        String raw = RestClient.create(BASE_URL)
                .post()
                .uri("/v1/tracking-links")
                .header("Authorization", "Bearer " + properties.getTrackingLinkApiToken())
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(String.class);

        log.info("Airbridge 응답: {}", raw);

        try {
            JsonNode root = MAPPER.readTree(raw);
            String shortUrl = root.path("data").path("trackingLink").path("shortUrl").asText(null);
            if (shortUrl == null || shortUrl.isBlank()) {
                throw new RuntimeException("shortUrl 없음. 응답: " + raw);
            }
            return shortUrl;
        } catch (Exception e) {
            throw new RuntimeException("Airbridge 트래킹 링크 파싱 실패: " + raw, e);
        }
    }

    @Getter
    @RequiredArgsConstructor
    static class CreateRequest {
        private final String channel;
        private final String deeplinkUrl;
        private final FallbackPaths fallbackPaths = new FallbackPaths("google-play");

        @Getter
        @RequiredArgsConstructor
        static class FallbackPaths {
            private final String android;
        }
    }
}
