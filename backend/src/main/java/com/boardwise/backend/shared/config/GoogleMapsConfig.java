package com.boardwise.backend.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.maps.GeoApiContext;

@Configuration
public class GoogleMapsConfig {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Bean
    public GeoApiContext geoApiContext(){
        return new GeoApiContext.Builder()
                        .apiKey(apiKey)
                        .build();
    }
}
