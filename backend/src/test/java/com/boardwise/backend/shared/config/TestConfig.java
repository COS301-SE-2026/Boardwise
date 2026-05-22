package com.boardwise.backend.shared.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {
    // Override the Seeding bean with a no-op
    @Bean
    public CommandLineRunner seedingNoOp() {
        return args -> {
        };
    }
}