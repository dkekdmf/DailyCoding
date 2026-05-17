package com.example.zero.healthcare.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "airbridge")
public class AirbridgeProperties {
    private String trackingLinkApiToken;
    private String appName;
}
