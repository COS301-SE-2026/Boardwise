package com.boardwise.backend.vault.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommitEditDeltaOrDoActionRequestDto extends VaultBaseRequestDto {
    private String chunkId;
}
