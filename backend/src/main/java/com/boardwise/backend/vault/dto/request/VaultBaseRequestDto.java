package com.boardwise.backend.vault.dto.request;

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
    private String content;
}
