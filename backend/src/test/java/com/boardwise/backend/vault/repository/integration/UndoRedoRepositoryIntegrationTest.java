package com.boardwise.backend.vault.repository.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.boardwise.backend.vault.VaultIntegrationTest;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.RulebookRepository;

public class UndoRedoRepositoryIntegrationTest extends VaultIntegrationTest {
    @Autowired
    private RulebookRepository rulebookRepository;
    
    @Nested
    class UndoRedoActions{
        ObjectId rulebookId;
        ObjectId userId;
        Rulebook rulebook;

        @BeforeEach
        void setup(){
            rulebookId = new ObjectId();
            userId = new ObjectId();

            rulebook = Rulebook.builder()
                .id(rulebookId)
                .lockHeldBy(userId)
                .undoStack(List.of(0L, 1L, 2L))
                .redoStack(List.of(5L, 4L, 3L))
                .build();

            rulebookRepository.save(rulebook);
        }

        @Test
        void atomicPopUndoAndPushRedoShouldSucceedIfUserHoldsTheLock(){            
            // Act
            Long popped = rulebookRepository.atomicPopUndoAndPushRedo(rulebookId, userId);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

            // Assert
            assertEquals(2L, popped, "The version popped from the undo stack should match exactly");
            assertEquals(2, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(2L, fetched.getRedoStack().getLast(), "The version pushed into the redo stack should match exactly");
            assertEquals(4, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
        }

        @Test
        void atomicPopUndoAndPushRedoShouldFailIfUserDoesNotHoldTheLock(){
            // Arrange
            ObjectId doesNotHoldIt = new ObjectId();

            // Act
            Long shouldBeNull = rulebookRepository.atomicPopUndoAndPushRedo(rulebookId, doesNotHoldIt);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

            // Assert
            assertNull(shouldBeNull, "The pop failed so no changes were made. Null is returned.");
            assertEquals(3, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(2L, fetched.getUndoStack().getLast(), "The version at the top of the undo stack should match exactly");
            assertEquals(3, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(3L, fetched.getRedoStack().getLast(), "The version at the top of the redo stack should match exactly");
        }

        @Test
        void atomicPopUndoAndPushRedoShouldFailIfUndoStackIsEmpty(){
            // Arrange
            Rulebook emptyUndoStack = Rulebook.builder()
                    .id(new ObjectId())
                    .lockHeldBy(userId)
                    .undoStack(List.of())
                    .redoStack(List.of(5L, 4L, 3L))
                    .build();

            rulebookRepository.save(emptyUndoStack);
            // Act
            Long shouldBeNull = rulebookRepository.atomicPopUndoAndPushRedo(emptyUndoStack.getId(), userId);
            Rulebook fetched = rulebookRepository.findById(emptyUndoStack.getId()).orElse(null);

            // Assert
            assertNull(shouldBeNull, "The pop failed so no changes were made. Null is returned.");
            assertEquals(0, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(3, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(3L, fetched.getRedoStack().getLast(), "The version at the top of the redo stack should match exactly");
        }

        @Test
        void atomicPopRedoAndPushUndoShouldSucceedIfUserHoldsTheLock() {
            // Act
            Long popped = rulebookRepository.atomicPopRedoAndPushUndo(rulebookId, userId);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

            // Assert
            assertEquals(3L, popped, "The version popped from the redo stack should match exactly");
            assertEquals(2, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(3L, fetched.getUndoStack().getLast(), "The version pushed into the undo stack should match exactly");
            assertEquals(4, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
        }

        @Test
        void atomicPopRedoAndPushUndoShouldFailIfUserDoesNotHoldTheLock() {
            // Arrange
            ObjectId doesNotHoldIt = new ObjectId();

            // Act
            Long shouldBeNull = rulebookRepository.atomicPopRedoAndPushUndo(rulebookId, doesNotHoldIt);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

            // Assert
            assertNull(shouldBeNull, "The pop failed so no changes were made. Null is returned.");
            assertEquals(3, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(2L, fetched.getUndoStack().getLast(), "The version at the top of the undo stack should match exactly");
            assertEquals(3, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(3L, fetched.getRedoStack().getLast(), "The version at the top of the redo stack should match exactly");
        }

        @Test
        void atomicPopRedoAndPushUndoShouldFailIfUndoStackIsEmpty() {
            // Arrange
            Rulebook emptyRedoStack = Rulebook.builder()
                    .id(new ObjectId())
                    .lockHeldBy(userId)
                    .undoStack(List.of(0L, 1L, 2L))
                    .redoStack(List.of())
                    .build();

            rulebookRepository.save(emptyRedoStack);
            // Act
            Long shouldBeNull = rulebookRepository.atomicPopRedoAndPushUndo(emptyRedoStack.getId(), userId);
            Rulebook fetched = rulebookRepository.findById(emptyRedoStack.getId()).orElse(null);

            // Assert
            assertNull(shouldBeNull, "The pop failed so no changes were made. Null is returned.");
            assertEquals(0, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(3, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(2L, fetched.getUndoStack().getLast(), "The version at the top of the undo stack should match exactly");
        }
    }
}
