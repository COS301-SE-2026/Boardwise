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

        ObjectId u1, u2, u3;
        ObjectId r1, r2, r3;

        @BeforeEach
        void setup(){
            rulebookId = new ObjectId();
            userId = new ObjectId();

            u1 = new ObjectId(); u2 = new ObjectId(); u3 = new ObjectId();
            r1 = new ObjectId(); r2 = new ObjectId(); r3 = new ObjectId();

            rulebook = Rulebook.builder()
                .id(rulebookId)
                .lockHeldBy(userId)
                .undoStack(List.of(u1, u2, u3))
                .redoStack(List.of(r1, r2, r3))
                .build();

            rulebookRepository.save(rulebook);
        }

        @Test
        void atomicPopUndoAndPushRedoShouldSucceedIfUserHoldsTheLock(){            
            // Act
            ObjectId popped = rulebookRepository.atomicPopUndoAndPushRedo(rulebookId, userId);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

            // Assert
            assertEquals(u3, popped, "The version popped from the undo stack should match exactly");
            assertEquals(2, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(u3, fetched.getRedoStack().getLast(), "The version pushed into the redo stack should match exactly");
            assertEquals(4, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
        }

        @Test
        void atomicPopUndoAndPushRedoShouldFailIfUserDoesNotHoldTheLock(){
            // Arrange
            ObjectId doesNotHoldIt = new ObjectId();

            // Act
            ObjectId shouldBeNull = rulebookRepository.atomicPopUndoAndPushRedo(rulebookId, doesNotHoldIt);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

            // Assert
            assertNull(shouldBeNull, "The pop failed so no changes were made. Null is returned.");
            assertEquals(3, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(u3, fetched.getUndoStack().getLast(), "The version at the top of the undo stack should match exactly");
            assertEquals(3, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(r3, fetched.getRedoStack().getLast(), "The version at the top of the redo stack should match exactly");
        }

        @Test
        void atomicPopUndoAndPushRedoShouldFailIfUndoStackIsEmpty(){
            // Arrange
            Rulebook emptyUndoStack = Rulebook.builder()
                    .id(new ObjectId())
                    .lockHeldBy(userId)
                    .undoStack(List.of())
                    .redoStack(List.of(r1, r2, r3))
                    .build();

            rulebookRepository.save(emptyUndoStack);
            // Act
            ObjectId shouldBeNull = rulebookRepository.atomicPopUndoAndPushRedo(emptyUndoStack.getId(), userId);
            Rulebook fetched = rulebookRepository.findById(emptyUndoStack.getId()).orElse(null);

            // Assert
            assertNull(shouldBeNull, "The pop failed so no changes were made. Null is returned.");
            assertEquals(0, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(3, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(r3, fetched.getRedoStack().getLast(), "The version at the top of the redo stack should match exactly");
        }

        @Test
        void atomicPopRedoAndPushUndoShouldSucceedIfUserHoldsTheLock() {
            // Act
            ObjectId popped = rulebookRepository.atomicPopRedoAndPushUndo(rulebookId, userId);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

            // Assert
            assertEquals(r3, popped, "The version popped from the redo stack should match exactly");
            assertEquals(2, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(r3, fetched.getUndoStack().getLast(), "The version pushed into the undo stack should match exactly");
            assertEquals(4, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
        }

        @Test
        void atomicPopRedoAndPushUndoShouldFailIfUserDoesNotHoldTheLock() {
            // Arrange
            ObjectId doesNotHoldIt = new ObjectId();

            // Act
            ObjectId shouldBeNull = rulebookRepository.atomicPopRedoAndPushUndo(rulebookId, doesNotHoldIt);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);

            // Assert
            assertNull(shouldBeNull, "The pop failed so no changes were made. Null is returned.");
            assertEquals(3, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(u3, fetched.getUndoStack().getLast(), "The version at the top of the undo stack should match exactly");
            assertEquals(3, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(r3, fetched.getRedoStack().getLast(), "The version at the top of the redo stack should match exactly");
        }

        @Test
        void atomicPopRedoAndPushUndoShouldFailIfUndoStackIsEmpty() {
            // Arrange
            Rulebook emptyRedoStack = Rulebook.builder()
                    .id(new ObjectId())
                    .lockHeldBy(userId)
                    .undoStack(List.of(u1, u2, u3))
                    .redoStack(List.of())
                    .build();

            rulebookRepository.save(emptyRedoStack);
            // Act
            ObjectId shouldBeNull = rulebookRepository.atomicPopRedoAndPushUndo(emptyRedoStack.getId(), userId);
            Rulebook fetched = rulebookRepository.findById(emptyRedoStack.getId()).orElse(null);

            // Assert
            assertNull(shouldBeNull, "The pop failed so no changes were made. Null is returned.");
            assertEquals(0, fetched.getRedoStack().size(), "The size of the redo stack must match exactly");
            assertEquals(3, fetched.getUndoStack().size(), "The size of the undo stack must match exactly");
            assertEquals(u3, fetched.getUndoStack().getLast(), "The version at the top of the undo stack should match exactly");
        }
    }
}
