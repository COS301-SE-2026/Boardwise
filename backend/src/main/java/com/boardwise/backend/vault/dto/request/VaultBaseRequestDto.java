package com.boardwise.backend.vault.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder // Allows for builder inheritance
@NoArgsConstructor
@AllArgsConstructor
public class VaultBaseRequestDto {
    private long expectedVersion;

    @Size(max = 1000, message = "Chunk content cannot exceed 1000 characters")
    private String content;
}
