package com.boardwise.backend.shared.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient bggRestClient(
        @Value("${bgg.token}") String token,
        @Value("${bgg.url}") String baseUrl
    ){
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    @Bean 
    public RestClient scraperRestClient(
    @Value("${scraper.service.url}") 
    String scraperUrl
    ){
        return RestClient.builder()
        .baseUrl(scraperUrl)
        .build();
    }

    @Bean
    public RestClient osmRestClient(
        @Value("${nominatim.service.url}") String openStreetBaseUrl
    ){
        return RestClient.builder()
                        .baseUrl(openStreetBaseUrl)
                        .defaultHeader("User-Agent", "BoardwiseSpringBackend/1.0 (worksonmymachine67@gmail.com)")
                        .build();
    }
}
