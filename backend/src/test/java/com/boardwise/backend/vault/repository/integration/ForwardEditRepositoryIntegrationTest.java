package com.boardwise.backend.vault.repository.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.boardwise.backend.vault.VaultIntegrationTest;
import com.boardwise.backend.vault.model.Chunk;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;

public class ForwardEditRepositoryIntegrationTest extends VaultIntegrationTest {
    @Autowired
    private RulebookRepository rulebookRepository;

    @Autowired
    private RulebookTextRepository rulebookTextRepository;

    @Test
    void AtomicCommitForwardEditShouldSucceedWhenGivenAValidRulebook(){
        // Arrange
        List<Long> undoStack = new ArrayList<>();
        for (long i = 0; i < 50; i++) {
            undoStack.add(i);
        }
        List<Long> redoStack = List.of(50L,51L,52L);
        ObjectId rulebookId = new ObjectId();

        Long newVersion = 53L;
        Rulebook rulebook = Rulebook.builder().id(rulebookId).undoStack(undoStack).redoStack(redoStack).build();
        rulebookRepository.save(rulebook);
        
        // Act
        rulebookRepository.atomicCommitForwardEdit(rulebookId, newVersion);
        Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

        // Assert
        assertEquals(newVersion, fetched.getUndoStack().getLast(), "The last appened element to the undo stack should be the new version");
        assertEquals(1L, fetched.getUndoStack().getFirst(), "The first element should match exactly as appending the new version should discard the 50th element");
        assertTrue(fetched.getRedoStack().isEmpty(), "Redo stack should be empty after every forward edit.");
    }

    @Nested
    class AtomicChunkActions{
        private RulebookText rulebookText;
        private ObjectId rulebookId;

        @BeforeEach
        void setup(){
            rulebookId = new ObjectId();
            List<Chunk> chunks = List.of(
                Chunk.builder().chunkId(new ObjectId()).index(0).content("Stuff at index 0").build(),
                Chunk.builder().chunkId(new ObjectId()).index(1).content("Stuff at index 1").build(),
                Chunk.builder().chunkId(new ObjectId()).index(2).content("Stuff at index 2").build()
            );

            rulebookText = RulebookText.builder().rulebookId(rulebookId).chunks(chunks).build();

            rulebookTextRepository.save(rulebookText);
        }

        @Test
        void atomicUpdateChunkShouldSucceedForValidChunkId(){
            // Act
            rulebookTextRepository.atomicUpdateChunk(rulebookId, rulebookText.getChunks().get(1).getChunkId(), "new content");
            RulebookText fetched = rulebookTextRepository.findByRulebookId(rulebookId).orElse(null);

            // Assert
            assertEquals("new content", fetched.getChunks().get(1).getContent(), "The content should match exactly");
        }
    }
}
