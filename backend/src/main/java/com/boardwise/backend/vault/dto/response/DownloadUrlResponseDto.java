package com.boardwise.backend.vault.dto.response;

import java.time.Instant;
import lombok.*;

@Data
@Builder
public class DownloadUrlResponseDto {
    private String downloadUrl;
    private Instant expiresAt;
}
