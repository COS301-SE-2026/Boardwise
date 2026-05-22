package com.boardwise.backend.vault;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.boardwise.backend.vault.dto.response.DownloadUrlResponseDto;
import com.boardwise.backend.vault.dto.response.EditHistoryResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookTextResponseDto;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;
import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;
import com.boardwise.backend.vault.repository.WriteLockRepository;
import com.boardwise.backend.vault.service.RulebookService;

@ExtendWith(MockitoExtension.class)
public class RulebookServiceTest {

        @Mock
        private RulebookRepository rulebookRepository;
        @Mock
        private RulebookTextRepository rulebookTextRepository;
        @Mock
        private WriteLockRepository writeLockRepository;
        @Mock
        private EditEventRepository editEventRepository;

        @InjectMocks
        private RulebookService rulebookService;

        private ObjectId rulebookId;
        private ObjectId contributorId;
        private ObjectId editorId;
        private Rulebook mockRulebook;
        private RulebookText mockRulebookText;
        private WriteLock mockWriteLock;
        private EditEvent mockEditEvent;

        @BeforeEach
        void setUp() {
                rulebookId = new ObjectId();
                contributorId = new ObjectId();
                editorId = new ObjectId();

                mockRulebook = Rulebook.builder()
                                .id(rulebookId)
                                .gameName("Catan")
                                .edition("3rd Edition")
                                .status("Ready")
                                .version(3)
                                .contributorId(contributorId)
                                .r2PdfKey("rulebooks/" + rulebookId.toHexString() + "/catan.pdf")
                                .uploadedAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();

                mockRulebookText = RulebookText.builder()
                                .id(new ObjectId())
                                .rulebookId(rulebookId)
                                .content("These are the rules for Catan.")
                                .version(3)
                                .updatedAt(Instant.now())
                                .build();

                mockWriteLock = WriteLock.builder()
                                .id(new ObjectId())
                                .rulebookId(rulebookId)
                                .heldByUserId(editorId)
                                .acquiredAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(30))
                                .build();

                mockEditEvent = EditEvent.builder()
                                .id(new ObjectId())
                                .rulebookId(rulebookId)
                                .editorId(editorId)
                                .delta("Added setup instructions.")
                                .versionAfter(3)
                                .committedAt(Instant.now())
                                .build();
        }

        // -------------------------------------------------------------------------
        // searchRulebooks
        // -------------------------------------------------------------------------

        @Test
        void searchRulebooks_returnsPageOfReadyRulebooks() {
                Page<Rulebook> mockPage = new PageImpl<>(List.of(mockRulebook));
                when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
                                eq("Ready"), eq("Catan"), any(Pageable.class))).thenReturn(mockPage);
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.empty());

                Page<RulebookResponseDto> result = rulebookService.searchRulebooks("Catan", 1, 20);

                assertEquals(1, result.getTotalElements());
                assertEquals("Catan", result.getContent().get(0).getGameName());
                assertEquals("Ready", result.getContent().get(0).getStatus());
                assertNull(result.getContent().get(0).getLockHeldBy());
        }

        @Test
        void searchRulebooks_returnsEmptyPageWhenNoMatch() {
                when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
                                eq("Ready"), eq("Unknown"), any(Pageable.class))).thenReturn(Page.empty());

                Page<RulebookResponseDto> result = rulebookService.searchRulebooks("Unknown", 1, 20);

                assertTrue(result.isEmpty());
        }

        // -------------------------------------------------------------------------
        // getRulebookById
        // -------------------------------------------------------------------------

        @Test
        void getRulebookById_returnsRulebookResponseDto() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.empty());

                RulebookResponseDto result = rulebookService.getRulebookById(rulebookId);

                assertEquals(rulebookId.toHexString(), result.getId());
                assertEquals("Catan", result.getGameName());
                assertNull(result.getLockHeldBy());
        }

        @Test
        void getRulebookById_returnsLockHeldByWhenLockExists() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(mockWriteLock));

                RulebookResponseDto result = rulebookService.getRulebookById(rulebookId);

                assertEquals(editorId.toHexString(), result.getLockHeldBy());
        }

        @Test
        void getRulebookById_throwsNotFoundForUnknownId() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

                assertThrows(RulebookNotFoundException.class,
                                () -> rulebookService.getRulebookById(rulebookId));
        }

        // -------------------------------------------------------------------------
        // getRulebookText
        // -------------------------------------------------------------------------

        @Test
        void getRulebookText_returnsTextResponseDto() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(rulebookTextRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(mockRulebookText));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.empty());

                RulebookTextResponseDto result = rulebookService.getRulebookText(rulebookId);

                assertEquals(rulebookId.toHexString(), result.getRulebookId());
                assertEquals("These are the rules for Catan.", result.getContent());
                assertEquals(3, result.getVersion());
                assertNull(result.getLockHeldBy());
        }

        @Test
        void getRulebookText_returnsLockHeldByWhenLockExists() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(rulebookTextRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(mockRulebookText));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(mockWriteLock));

                RulebookTextResponseDto result = rulebookService.getRulebookText(rulebookId);

                assertEquals(editorId.toHexString(), result.getLockHeldBy());
        }

        @Test
        void getRulebookText_throwsNotFoundWhenRulebookMissing() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

                assertThrows(RulebookNotFoundException.class,
                                () -> rulebookService.getRulebookText(rulebookId));
        }

        @Test
        void getRulebookText_throwsNotFoundWhenTextMissing() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(rulebookTextRepository.findByRulebookId(rulebookId)).thenReturn(Optional.empty());

                assertThrows(RulebookNotFoundException.class,
                                () -> rulebookService.getRulebookText(rulebookId));
        }

        // -------------------------------------------------------------------------
        // getDownloadUrl
        // -------------------------------------------------------------------------

        @Test
        void getDownloadUrl_returnsDownloadUrlResponseDto() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));

                DownloadUrlResponseDto result = rulebookService.getDownloadUrl(rulebookId);

                assertNotNull(result.getDownloadUrl());
                assertNotNull(result.getExpiresAt());
        }

        @Test
        void getDownloadUrl_throwsNotFoundWhenR2KeyIsNull() {
                Rulebook noKeyRulebook = Rulebook.builder()
                                .id(rulebookId)
                                .gameName("Catan")
                                .status("Ready")
                                .version(1)
                                .contributorId(contributorId)
                                .r2PdfKey(null)
                                .uploadedAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();

                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(noKeyRulebook));

                assertThrows(RulebookNotFoundException.class,
                                () -> rulebookService.getDownloadUrl(rulebookId));
        }

        // -------------------------------------------------------------------------
        // getEditHistory
        // -------------------------------------------------------------------------

        @Test
        void getEditHistory_returnsChronologicalEditEvents() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId))
                                .thenReturn(List.of(mockEditEvent));

                EditHistoryResponseDto result = rulebookService.getEditHistory(rulebookId);

                assertEquals(rulebookId.toHexString(), result.getRulebookId());
                assertEquals(1, result.getTotalEdits());
                assertEquals(editorId.toHexString(), result.getEdits().get(0).getEditorId());
                assertEquals("Added setup instructions.", result.getEdits().get(0).getDelta());
        }

        @Test
        void getEditHistory_returnsEmptyListForNewRulebook() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId))
                                .thenReturn(List.of());

                EditHistoryResponseDto result = rulebookService.getEditHistory(rulebookId);

                assertEquals(0, result.getTotalEdits());
                assertTrue(result.getEdits().isEmpty());
        }

        @Test
        void getEditHistory_throwsNotFoundForUnknownRulebook() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

                assertThrows(RulebookNotFoundException.class,
                                () -> rulebookService.getEditHistory(rulebookId));
        }
}