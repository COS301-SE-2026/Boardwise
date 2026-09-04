package com.boardwise.backend.vault.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.boardwise.backend.vault.dto.request.ReEmbedRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiGatewayWebhookService {
    private final WebClient aiGatewayWebClient;

    public Mono<Void> triggerReEmbedding(String chunkId, String content, Map<String, String> metadata){
        ReEmbedRequestDto payload = new ReEmbedRequestDto(chunkId, content, metadata);

        return aiGatewayWebClient.post()
            .uri("vault/internal/chunks/re-embed")
            .bodyValue(payload)
            .retrieve()
            .toBodilessEntity()
            .doOnSuccess(response -> log.info("Successfully queued re-embedding for chunk: {}", chunkId))
            .onErrorResume(error -> {log.info("Failed to queue webhook for chunk {}: {}", chunkId, error.getMessage());
            return Mono.empty();})
            .then();
    }
}
