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

        @Test
        void atomicInsertChunkShouldSucceedForValidInsertIndex(){
            // Arrange
            String newContent = "This is being added";
            int insertIndex = 1;

            // Act
            rulebookTextRepository.atomicInsertChunk(rulebookId, new ObjectId(), newContent, insertIndex);
            RulebookText fetched = rulebookTextRepository.findByRulebookId(rulebookId).orElse(null);

            // Assert
            assertEquals(4, fetched.getChunks().size(), "The number of chunks should match exactly");
            assertEquals(insertIndex, fetched.getChunks().get(insertIndex).getIndex(), "The insert index should match exactly");
            assertEquals(newContent, fetched.getChunks().get(insertIndex).getContent(), "The content should match exactly");
        }
        
        @Test
        void atomicInsertChunkShouldSucceedForInvalidInsertIndexAndAppendsChunkToEndOfList(){
            // Arrange
            String newContent = "This is being added";

            // Act
            rulebookTextRepository.atomicInsertChunk(rulebookId, new ObjectId(), newContent, -5);
            RulebookText fetched = rulebookTextRepository.findByRulebookId(rulebookId).orElse(null);

            // Assert
            assertEquals(4, fetched.getChunks().size(), "The number of chunks should match exactly");
            assertEquals(3, fetched.getChunks().getLast().getIndex(), "The insert index should match exactly");
            assertEquals(newContent, fetched.getChunks().getLast().getContent(), "The content should match exactly");
        }
        
        @Test
        void atomicInsertChunkShouldSucceedForAbsentChunkId(){
            // Arrange
            String newContent = "This is being added";
            int insertIndex = 2;

            // Act
            rulebookTextRepository.atomicInsertChunk(rulebookId, null, newContent, insertIndex);
            RulebookText fetched = rulebookTextRepository.findByRulebookId(rulebookId).orElse(null);

            // Assert
            assertEquals(4, fetched.getChunks().size(), "The number of chunks should match exactly");
            assertEquals(insertIndex, fetched.getChunks().get(insertIndex).getIndex(),
                    "The insert index should match exactly");
            assertEquals(newContent, fetched.getChunks().get(insertIndex).getContent(),
                    "The content should match exactly");
        }

        @Test
        void atomicDeleteChunkShouldSucceedForValidChunkId(){
            // Arrange
            ObjectId validChunkId = rulebookText.getChunks().get(1).getChunkId();
            
            // Act
            boolean res = rulebookTextRepository.atomicDeleteChunk(rulebookId, validChunkId);
            RulebookText fetched = rulebookTextRepository.findByRulebookId(rulebookId).orElse(null);

            // Assert
            assertTrue(res, "The boolean flag should be true when chunk deletion succeeds");
            assertEquals(2, fetched.getChunks().size(), "The number of chunks should match exactly");
            assertEquals(1, fetched.getChunks().get(1).getIndex(), "The index should match exactly");
        }
        
        @Test
        void atomicDeleteChunkShouldFailForInvalidChunkId(){
            // Arrange
            ObjectId invalidChunkId = new ObjectId();

            // Act
            boolean res = rulebookTextRepository.atomicDeleteChunk(rulebookId, invalidChunkId);
            RulebookText fetched = rulebookTextRepository.findByRulebookId(rulebookId).orElse(null);

            // Assert
            assertFalse(res, "The boolean flag should be false when chunk deletion fails");
            assertEquals(3, fetched.getChunks().size(), "The number of chunks should match exactly");
        }
    }
}
