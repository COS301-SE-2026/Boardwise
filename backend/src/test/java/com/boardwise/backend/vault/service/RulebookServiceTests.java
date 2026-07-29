package com.boardwise.backend.vault.service;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookSummaryResponseDto;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ExtendWith(MockitoExtension.class)
public class RulebookServiceTests {
    @Mock
    private RulebookRepository rulebookRepository;

    @Mock
    private RulebookTextRepository rulebookTextRepository;

    @Mock
    private EditEventRepository editEventRepository;

    @Mock
    private BoardGameRepository boardgameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private RulebookService rulebookService;

    @BeforeEach
    void setup(){
        ReflectionTestUtils.setField(rulebookService, "r2PublicDomain", "https://cdn.mock.com");
        ReflectionTestUtils.setField(rulebookService, "rulebooksBucket", "rulebooks-bucket");
    }
    
    // ---------- AC-VLT-02: List/ Search Rulebooks ----------
    @Nested
    class SearchRulebooksTests{
        private Rulebook rb = null;

        @BeforeEach
        void setUp(){
            rb = Rulebook.builder()
                .id(new ObjectId())
                .coverUrl("https://covers.mock.com/catan.jpg")
                .title("Catan")
                .edition("5th")
                .genres(List.of("Strategy"))
                .version(1L)
                .status("Ready")
                .contributorId(new ObjectId())
                .contributorUsername("MockUser")
                .description("Mocked Description")
                .language("English")
                .r2PdfKey("mock-pdf-key")
                .r2CoverKey("mock-cover-key")
                .lockHeldBy(null)
                .lockExpiresAt(null)
                .undoStack(List.of())
                .redoStack(List.of())
                .uploadedAt(Instant.now())
                .updatedAt(Instant.now())
                .minPlayers(3)
                .maxPlayers(4)
                .duration(90)
                .minAge(10)
                .build();
        }

        @Test
        public void testSearchRulebooksBuildsZeroIndexedPageableSortedByUpdatedAtDesc(){
            // Arrange
            Page<Rulebook> emptyPage = new PageImpl<>(List.of());
            when(rulebookRepository.searchWithFilters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Pageable.class))).thenReturn(emptyPage);

            // Act
            rulebookService.searchRulebooks("catan", "strategy", List.of("English"), 4, 90, 10, 2, 20);

            // Assert
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(rulebookRepository).searchWithFilters(
                    Mockito.eq("catan"), Mockito.eq("strategy"), Mockito.eq(List.of("English")),
                    Mockito.eq(4), Mockito.eq(90), Mockito.eq(10), pageableCaptor.capture());
            
            Pageable captured = pageableCaptor.getValue();
            Assertions.assertThat(captured.getPageNumber()).isEqualTo(1);
            Assertions.assertThat(captured.getPageSize()).isEqualTo(20);
            Assertions.assertThat(captured.getSort().getOrderFor("updatedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        public void testSearchRulebooksCapsLimitAt100(){
            // Arrange
            Page<Rulebook> emptyPage = new PageImpl<>(List.of());
            when(rulebookRepository.searchWithFilters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Pageable.class))).thenReturn(emptyPage);
            
            // Act
            rulebookService.searchRulebooks( null, null, null, null, null, null, 1, 500);

            // Assert
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(rulebookRepository).searchWithFilters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), pageableCaptor.capture());

            Pageable captured = pageableCaptor.getValue();
            Assertions.assertThat(captured.getPageSize()).isEqualTo(100);
        }

        @Test
        public void testSearchRulebooksMapsRulebookFieldsToSummaryDto(){
            // Arrange
            Page<Rulebook> page = new PageImpl<>(List.of(rb));
            when(rulebookRepository.searchWithFilters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Pageable.class))).thenReturn(page);
            
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks(null, null, null, null, null, null, 1, 20);

            // Assert
            Assertions.assertThat(result.getContent()).hasSize(1);
            RulebookSummaryResponseDto dto = result.getContent().get(0);
            Assertions.assertThat(dto.getId()).isEqualTo(rb.getId().toHexString());
            Assertions.assertThat(dto.getCoverUrl()).isEqualTo(rb.getCoverUrl());
            Assertions.assertThat(dto.getTitle()).isEqualTo(rb.getTitle());
            Assertions.assertThat(dto.getEdition()).isEqualTo(rb.getEdition());
            Assertions.assertThat(dto.getGenres()).isEqualTo(rb.getGenres());
            Assertions.assertThat(dto.getVersion()).isEqualTo(rb.getVersion());
            Assertions.assertThat(dto.getLanguage()).isEqualTo(rb.getLanguage());
            Assertions.assertThat(dto.getMinPlayers()).isEqualTo(rb.getMinPlayers());
            Assertions.assertThat(dto.getMaxPlayers()).isEqualTo(rb.getMaxPlayers());
            Assertions.assertThat(dto.getDuration()).isEqualTo(rb.getDuration());
            Assertions.assertThat(dto.getMinAge()).isEqualTo(rb.getMinAge());
        }

        @Test
        public void testSearchRulebooksPageZeroThrowsIllegalArgumentException(){
            Assertions.assertThatThrownBy(() -> rulebookService.searchRulebooks(null, null, null, null, null, null, 0, 20))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
    // ---------- AC-VLT-03: Get Rulebook Detail ----------
    @Nested
    class GetRulebookByIdTests{
        private ObjectId rulebookId = null;
        private Rulebook rb = null;

        @BeforeEach
        void setUp(){
            rulebookId = new ObjectId();
            rb = Rulebook.builder()
                .id(rulebookId)
                .coverUrl("https://covers.mock.com/catan.jpg")
                .title("Catan")
                .edition("5th")
                .genres(List.of("Strategy"))
                .version(1L)
                .status("Ready")
                .contributorId(new ObjectId())
                .contributorUsername("MockUser")
                .description("Mocked Description")
                .language("English")
                .r2PdfKey("mock-pdf-key")
                .r2CoverKey("mock-cover-key")
                .lockHeldBy(null)
                .lockExpiresAt(null)
                .undoStack(List.of())
                .redoStack(List.of())
                .uploadedAt(Instant.now())
                .updatedAt(Instant.now())
                .minPlayers(3)
                .maxPlayers(4)
                .duration(90)
                .minAge(10)
                .build();
        }

        @Test
        public void testGetRulebookByIdThrowsWhenRulebookNotFound(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());
            
            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getRulebookById(rulebookId))
                .isInstanceOf(RulebookNotFoundException.class);
            
            Mockito.verifyNoInteractions(userRepository);
        }

        @Test
        public void testGetRulebookByIdMapsAllFieldsWhenNoLockHeld(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));
            // Act
            RulebookResponseDto dto = rulebookService.getRulebookById(rulebookId);

            // Assert
            Assertions.assertThat(dto.getId()).isEqualTo(rb.getId().toHexString());
            Assertions.assertThat(dto.getTitle()).isEqualTo(rb.getTitle());
            Assertions.assertThat(dto.getEdition()).isEqualTo(rb.getEdition());
            Assertions.assertThat(dto.getGenres()).isEqualTo(rb.getGenres());
            Assertions.assertThat(dto.getVersion()).isEqualTo(rb.getVersion());
            Assertions.assertThat(dto.getStatus()).isEqualTo(rb.getStatus());
            Assertions.assertThat(dto.getContributorUsername()).isEqualTo(rb.getContributorUsername());
            Assertions.assertThat(dto.getDescription()).isEqualTo(rb.getDescription());
            Assertions.assertThat(dto.getLanguage()).isEqualTo(rb.getLanguage());
            Assertions.assertThat(dto.getLockHeldBy()).isEmpty();
            Assertions.assertThat(dto.getLockExpiresAt()).isEqualTo(rb.getLockExpiresAt());
            Assertions.assertThat(dto.getUploadedAt()).isEqualTo(rb.getUploadedAt());
            Assertions.assertThat(dto.getUpdatedAt()).isEqualTo(rb.getUpdatedAt());
            Assertions.assertThat(dto.getMinPlayers()).isEqualTo(rb.getMinPlayers());
            Assertions.assertThat(dto.getMaxPlayers()).isEqualTo(rb.getMaxPlayers());
            Assertions.assertThat(dto.getDuration()).isEqualTo(rb.getDuration());
            Assertions.assertThat(dto.getMinAge()).isEqualTo(rb.getMinAge());

            Mockito.verifyNoInteractions(userRepository);
        }

        @Test
        public void testGetRulebookByIdResolvesLockHolderUsernameWhenLockHeld(){
            // Arrange
            ObjectId lockHolderId = new ObjectId();
            rb.setLockHeldBy(lockHolderId);
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            User user = new User();
            user.setId(lockHolderId.toHexString());
            user.setUsername("mockUser");
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            // Act
            RulebookResponseDto dto = rulebookService.getRulebookById(rulebookId);

            // Assert
            Assertions.assertThat(dto.getLockHeldBy()).isEqualTo(user.getUsername());
        }

        @Test
        public void testGetRulebookByIdThrowsWhenLockHolderUserMissing(){
            // Arrange
            ObjectId lockHolderId = new ObjectId();
            rb.setLockHeldBy(lockHolderId);
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));
            when(userRepository.findById(lockHolderId.toHexString())).thenReturn(Optional.empty());
            
            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getRulebookById(rulebookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User does not exist.");
        }

        @Nested
        class CoverUrlResolutionTests{
            @Test
            public void testGetRulebookByIdUsesCoverImageUrlWhenPresent() {
                // Arrange
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

                // Act
                RulebookResponseDto dto = rulebookService.getRulebookById(rulebookId);

                // Assert
                Assertions.assertThat(dto.getCoverUrl()).isEqualTo(rb.getCoverUrl());
            }

            @Test
            public void testGetRulebookByIdFallsBackToR2WhenCoverUrlBlank() {
                // Arrange
                rb.setCoverUrl("");
                rb.setR2CoverKey("catan.png");
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

                // Act
                RulebookResponseDto dto = rulebookService.getRulebookById(rulebookId);

                // Assert
                Assertions.assertThat(dto.getCoverUrl()).isEqualTo("https://cdn.mock.com/catan.png");
            }

            @Test
            public void testGetRulebookByIdStripsLeadingSlashFromR2CoverKey() {
                // Arrange
                rb.setCoverUrl("");
                rb.setR2CoverKey("/catan.png");
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

                // Act
                RulebookResponseDto dto = rulebookService.getRulebookById(rulebookId);

                // Assert
                Assertions.assertThat(dto.getCoverUrl()).isEqualTo("https://cdn.mock.com/catan.png");
            }

            @Test
            public void testGetRulebookByIdNullCoverUrlThrowsNullPointerException() {
                // Arrange
                rb.setCoverUrl(null);
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

                // Act and Assert
                Assertions.assertThatThrownBy(() -> rulebookService.getRulebookById(rulebookId))
                        .isInstanceOf(NullPointerException.class);
            }
        }

        @Test
        public void testGetRulebookByIdMapsSentinelMinusOneForMissingPlayerCounts(){
            // Arrange
            rb.setMinPlayers(-1);
            rb.setMaxPlayers(-1);
            rb.setMinAge(-1);
            rb.setDuration(-1);
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            // Act
            RulebookResponseDto dto = rulebookService.getRulebookById(rulebookId);

            // Assert
            Assertions.assertThat(dto.getMinPlayers()).isEqualTo(-1);
            Assertions.assertThat(dto.getMaxPlayers()).isEqualTo(-1);
            Assertions.assertThat(dto.getMinAge()).isEqualTo(-1);
            Assertions.assertThat(dto.getDuration()).isEqualTo(-1);
        }
    }

    // ---------- AC-VLT-04: Download Raw PDF ----------
    @Nested
    class GetDownloadUrlTests{}

    // ---------- AC-VLT-05: Get Rulebook Text State ----------
    @Nested
    class GetRulebookTextTests{}

    // ---------- AC-VLT-09: Get Rulebook Edit History ----------
    @Nested
    class GetEditHistoryTests{}

    @Nested
    class CoverUrlResolutionTests{}
}