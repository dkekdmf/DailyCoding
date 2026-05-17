package com.example.zero.healthcare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ttamtamOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ttamtam Healthcare API")
                        .description("PT·운동 일지 관리 앱 백엔드 API")
                        .version("v1"))
                .addServersItem(new Server().url("http://localhost:8080").description("Local"));
    }
}
