package com.boardwise.backend.vault.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InsertNewChunkRequestDto extends VaultBaseRequestDto {
    private int insertIndex;
}
