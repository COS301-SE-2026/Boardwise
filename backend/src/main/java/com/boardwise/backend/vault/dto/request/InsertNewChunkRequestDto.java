package com.boardwise.backend.vault.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsertNewChunkRequestDto {
    private int expectedVersion;
    private int insertIndex;
    private int lastIndex;
    private String content;
}
