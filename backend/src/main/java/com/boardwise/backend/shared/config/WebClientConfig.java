package com.boardwise.backend.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient aiGatewayWebClient(
        @Value("${ai.gateway.url}") String gatewayUrl,
        @Value("${ai.gateway.internal-secret}") String internalSecret){
        return WebClient.builder()
            .baseUrl(gatewayUrl)
            .defaultHeader("X-Internal-Token", internalSecret)
            .build();
    }
}
