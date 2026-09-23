package com.boardwise.scrapers.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    
    private static JdkClientHttpRequestFactory factory(Duration readTimeout){
        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    @Bean
    public RestClient externalRulebookClient(
        @Value("${rulebook.pdf.api.url}") String baseUrl
    ){
        System.out.print(baseUrl);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory(Duration.ofSeconds(60)))
                .build();
    }

    @Bean
    public RestClient pythonUploadClient(
        @Value("${python.api.base.url}") String baseUrl
    ) {
        return RestClient.builder()
            .requestFactory(factory(Duration.ofMinutes(15)))
            .baseUrl(baseUrl)
            .build();
    }
}
