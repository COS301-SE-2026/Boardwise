package com.boardwise.backend.shared.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class R2Config {

    @Value("${r2.access-key}")
    private String accessKey;

    @Value("${r2.secret-key}")
    private String secretKey;

    @Value("${r2.account-id}")
    private String accountId;

    @Bean
    public S3Client s3Client(){
        
        S3Configuration serviceConfig = S3Configuration.builder()
            .pathStyleAccessEnabled(true)    // Critical for R2
            .chunkedEncodingEnabled(false)   // Critical for R2 with Java SDK
            .build();

        return S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(
                    String.format("https://%s.r2.cloudflarestorage.com", accountId)))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(serviceConfig) // Apply the settings here
                .build();
    }
}
