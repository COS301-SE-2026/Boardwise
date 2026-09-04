package com.boardwise.backend.vault.dto.request;

import java.util.Map;

public record ReEmbedRequestDto(String chunkId, String content, Map<String, String> metadata){}
