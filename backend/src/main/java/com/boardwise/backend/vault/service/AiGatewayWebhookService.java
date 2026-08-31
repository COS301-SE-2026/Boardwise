package com.boardwise.backend.vault.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.boardwise.backend.vault.dto.request.ReEmbedRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiGatewayWebhookService {
    private final WebClient aiGatewayWebClient;

    public void triggerReEmbedding(String chunkId, String content){
        ReEmbedRequestDto payload = new ReEmbedRequestDto(chunkId, content);

        aiGatewayWebClient.post()
            .uri("vault/internal/chunks/re-embed")
            .bodyValue(payload)
            .retrieve()
            .toBodilessEntity()
            .subscribe(
                response -> log.info("Successfully queued re-embedding for chunk: {}", chunkId),
                error -> log.info("Failed to queue webhook for chunk {}: {}", chunkId, error.getMessage())
            );
    }
}
