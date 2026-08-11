package com.boardwise.backend.vault.repository.integration;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.boardwise.backend.SharedMongoContainer;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.RulebookRepository;

@DataMongoTest
public class RulebookRepositoryIntegrationTest extends SharedMongoContainer {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RulebookRepository rulebookRepository;

    @BeforeEach
    void setup(){
        mongoTemplate.dropCollection(Rulebook.class);
    }

    @Test
    void searchWithFiltersShouldFilterByPlayerCountAndStatusReady(){
        // Arrange
        Rulebook validRulebook = Rulebook.builder()
            .title("Catan")
            .status("Ready")
            .minPlayers(3)
            .maxPlayers(4)
            .duration(90)
            .build();
        Rulebook processingRulebook = Rulebook.builder()
            .title("Some game")
            .status("Processing")
            .minPlayers(2)
            .maxPlayers(5)
            .build();
        mongoTemplate.save(validRulebook);
        mongoTemplate.save(processingRulebook);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<Rulebook> result = rulebookRepository.searchWithFilters(null, null, null, 3, null, null, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Catan");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void searchWithFiltersShouldApplyCaseInsensitiveRegexSearch(){
        // Arrange
        mongoTemplate.save(Rulebook.builder().title("Ticket to Ride").status("Ready").build());
        
        // Act
        Page<Rulebook> result = rulebookRepository.searchWithFilters("ticket", null, null, null, null, null, PageRequest.of(0,10));
        
        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Ticket to Ride");
    }
}
