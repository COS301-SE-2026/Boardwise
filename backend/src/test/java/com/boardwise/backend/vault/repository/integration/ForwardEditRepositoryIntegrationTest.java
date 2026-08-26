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
        private ObjectId rulebookId;
        private List<RulebookText> chunks = null;

        @BeforeEach
        void setup(){
            rulebookId = new ObjectId();
            chunks = List.of(
                RulebookText.builder().rulebookId(rulebookId).chunkId(new ObjectId()).index(0).content("Stuff at index 0").build(),
                RulebookText.builder().rulebookId(rulebookId).chunkId(new ObjectId()).index(1).content("Stuff at index 1").build(),
                RulebookText.builder().rulebookId(rulebookId).chunkId(new ObjectId()).index(2).content("Stuff at index 2").build()
            );

            rulebookTextRepository.saveAll(chunks);
        }

        @Test
        void atomicUpdateChunkShouldSucceedForValidChunkId(){
            // Act
            rulebookTextRepository.atomicUpdateChunk(rulebookId, chunks.get(1).getChunkId(), "new content");
            List<RulebookText> fetched = rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId);

            // Assert
            assertEquals("new content", fetched.get(1).getContent(), "The content should match exactly");
        }

        @Test
        void atomicInsertChunkShouldSucceedForValidInsertIndex(){
            // Arrange
            String newContent = "This is being added";
            int insertIndex = 1;

            // Act
            rulebookTextRepository.atomicInsertChunk(rulebookId, newContent, insertIndex);
            List<RulebookText> fetched = rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId);

            // Assert
            assertEquals(4, fetched.size(), "The number of chunks should match exactly");
            assertEquals(insertIndex, fetched.get(insertIndex).getIndex(), "The insert index should match exactly");
            assertEquals(newContent, fetched.get(insertIndex).getContent(), "The content should match exactly");
        }
        
        @Test
        void atomicInsertChunkShouldSucceedForInvalidInsertIndexAndAppendsChunkToEndOfList(){
            // Arrange
            String newContent = "This is being added";

            // Act
            rulebookTextRepository.atomicInsertChunk(rulebookId, newContent, -5);
            List<RulebookText> fetched = rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId);

            // Assert
            assertEquals(4, fetched.size(), "The number of chunks should match exactly");
            assertEquals(3, fetched.getLast().getIndex(), "The insert index should match exactly");
            assertEquals(newContent, fetched.getLast().getContent(), "The content should match exactly");
        }

        @Test
        void atomicDeleteChunkShouldSucceedForValidChunkId(){
            // Arrange
            ObjectId validChunkId = chunks.get(1).getChunkId();
            
            // Act
            boolean res = rulebookTextRepository.atomicDeleteChunk(rulebookId, validChunkId);
            List<RulebookText> fetched = rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId);

            // Assert
            assertTrue(res, "The boolean flag should be true when chunk deletion succeeds");
            assertEquals(2, fetched.size(), "The number of chunks should match exactly");
            assertEquals(1, fetched.get(1).getIndex(), "The index should match exactly");
        }
        
        @Test
        void atomicDeleteChunkShouldFailForInvalidChunkId(){
            // Arrange
            ObjectId invalidChunkId = new ObjectId();

            // Act
            boolean res = rulebookTextRepository.atomicDeleteChunk(rulebookId, invalidChunkId);
            List<RulebookText> fetched = rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId);

            // Assert
            assertFalse(res, "The boolean flag should be false when chunk deletion fails");
            assertEquals(3, fetched.size(), "The number of chunks should match exactly");
        }
    }
}
