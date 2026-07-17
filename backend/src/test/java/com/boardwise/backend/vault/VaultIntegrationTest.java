package com.boardwise.backend.vault;

import org.junit.jupiter.api.BeforeEach;

import com.boardwise.backend.BaseIntegrationTest;
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;

public abstract class VaultIntegrationTest extends BaseIntegrationTest{
    @BeforeEach
    void cleanVaultDatabase(){
        mongoTemplate.dropCollection(Rulebook.class);
        mongoTemplate.dropCollection(RulebookText.class);
        mongoTemplate.dropCollection(EditEvent.class);
    }
}
