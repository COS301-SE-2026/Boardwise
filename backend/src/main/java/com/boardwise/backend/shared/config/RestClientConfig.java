package com.boardwise.backend.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient bggRestClient(
        
        @Value("${bgg.url}") String baseUrl
    ){
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
