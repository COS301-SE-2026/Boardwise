package com.boardwise.backend.vault.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InsertNewChunkResponseDto extends BaseColabResponseDto {
    private String chunkId;
    private int actualIndex;
}
