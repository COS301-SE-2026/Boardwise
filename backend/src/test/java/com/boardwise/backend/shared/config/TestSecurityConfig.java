package com.boardwise.backend.shared.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    public TestSecurityConfig() {
        System.out.println("DEBUG: TestSecurityConfig is LOADED!");
    }

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        System.out.println("DEBUG: testFilterChain configured");
        http
                .csrf(csrf -> csrf.disable()) // Explicitly disabled here
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}