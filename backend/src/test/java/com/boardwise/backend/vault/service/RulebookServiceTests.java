package com.boardwise.backend.vault.service;

import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
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

import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.vault.dto.response.ChunkDto;
import com.boardwise.backend.vault.dto.response.DownloadUrlResponseDto;
import com.boardwise.backend.vault.dto.response.EditEventResponseDto;
import com.boardwise.backend.vault.dto.response.EditHistoryResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookSummaryResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookTextResponseDto;
import com.boardwise.backend.vault.enums.EditType;
import com.boardwise.backend.vault.exception.R2PresignException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

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

    private Rulebook validRulebook(){
        return Rulebook.builder()
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

    private User userWithUsername(String id, String name){
        User user = new User();
        user.setId(id);
        user.setUsername(name);
        return user;
    }
    
    // ---------- AC-VLT-02: List/ Search Rulebooks ----------
    @Nested
    class SearchRulebooksTests{
        private Rulebook rb = null;

        @BeforeEach
        void setUp(){
            rb = validRulebook();
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
        void setUp() {
            rb = validRulebook();
            rulebookId = rb.getId();
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

            User user = userWithUsername(lockHolderId.toHexString(), "mockUser");
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
    class GetDownloadUrlTests{
        private ObjectId rulebookId = null;
        private Rulebook rb = null;

        @BeforeEach
        void setUp() {
            rb = validRulebook();
            rulebookId = rb.getId();
            rb.setR2PdfKey("pdfs/catan-rules.pdf");
        }

        @Test
        public void testGetDownloadUrlThrowsWhenRulebookNotFound(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getDownloadUrl(rulebookId))
                .isInstanceOf(RulebookNotFoundException.class);
            Mockito.verifyNoInteractions(s3Presigner);
        }
        
        @Test
        public void testGetDownloadUrlThrowsWhenPdfKeyIsNull(){
            // Arrange
            rb.setR2PdfKey(null);
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getDownloadUrl(rulebookId))
                .isInstanceOf(RulebookNotFoundException.class)
                .hasMessageContaining("PDF not yet available");
            Mockito.verifyNoInteractions(s3Presigner);
        }

        @Test
        public void testGetDownloadUrlReturnsPresignedUrlOnHappyPath() throws MalformedURLException, URISyntaxException{
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
            when(presigned.url()).thenReturn(new URI("https://r2.mock.com/signed?sig=abc").toURL());
            when(s3Presigner.presignGetObject(Mockito.any(GetObjectPresignRequest.class)))
                .thenReturn(presigned);
            // Act
            Instant before = Instant.now();
            DownloadUrlResponseDto dto = rulebookService.getDownloadUrl(rulebookId);
            Instant after = Instant.now();

            // Assert
            Assertions.assertThat(dto.getDownloadUrl()).isEqualTo("https://r2.mock.com/signed?sig=abc");
            Assertions.assertThat(dto.getExpiresAt()).isBetween(before.plusSeconds(295), after.plusSeconds(305));
        }

        @Test
        public void testGetDownloadUrlBuildsPresignRequestWithCorrectBucketKeyAndDuration() throws MalformedURLException, URISyntaxException{
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
            when(presigned.url()).thenReturn(new URI("https://r2.mock.com/signed").toURL());
            when(s3Presigner.presignGetObject(Mockito.any(GetObjectPresignRequest.class)))
                .thenReturn(presigned);

            // Act
            rulebookService.getDownloadUrl(rulebookId);

            // Assert
            ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
            Mockito.verify(s3Presigner).presignGetObject(captor.capture());

            GetObjectPresignRequest captured = captor.getValue();
            Assertions.assertThat(captured.signatureDuration()).isEqualTo(Duration.ofMinutes(5));
            Assertions.assertThat(captured.getObjectRequest().bucket()).isEqualTo("rulebooks-bucket");
            Assertions.assertThat(captured.getObjectRequest().key()).isEqualTo(rb.getR2PdfKey());
        }

        @Test
        public void testGetDownloadUrlWrapsPresignerExceptionInR2PresignException(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            when(s3Presigner.presignGetObject(Mockito.any(GetObjectPresignRequest.class)))
                .thenThrow(S3Exception.builder().message("Simulated S3 failure").statusCode(403)
                .awsErrorDetails(AwsErrorDetails.builder().errorCode("AccessDenied").errorMessage("User is not authorized to perform this action").build())
                .build());
            
            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getDownloadUrl(rulebookId))
                .isInstanceOf(R2PresignException.class)
                .hasMessageContaining("Simulated S3 failure");
        }
    }

    // ---------- AC-VLT-05: Get Rulebook Text State ----------
    @Nested
    class GetRulebookTextTests{
        private ObjectId rulebookId = null;
        private Rulebook rb = null;

        @BeforeEach
        void setUp() {
            rb = validRulebook();
            rulebookId = rb.getId();
        }

        @Test
        public void testGetRulebookTextThrowsWhenTulebookNotFound(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getRulebookText(rulebookId))
                .isInstanceOf(RulebookNotFoundException.class);
            Mockito.verifyNoInteractions(userRepository);
        }
        
        @Test
        public void testGetRulebookTextThrowsWhenTextContentNotFound(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));
            when(rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId)).thenReturn(List.of());

            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getRulebookText(rulebookId))
                .isInstanceOf(RulebookNotFoundException.class)
                .hasMessageContaining("Text content not found");
        }
        
        @Test
        public void testGetRulebookTextMapsChunksVersionAndUpdatedAtWhenNoLockHeld(){
            // Arrange
            rb.setVersion(3);
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            List<RulebookText> text = List.of(
                RulebookText.builder().rulebookId(rulebookId).chunkId(new ObjectId()).index(0).content("Setup: place the board...").createdAt(Instant.now()).updatedAt(rb.getUpdatedAt()).build(),
                RulebookText.builder().rulebookId(rulebookId).chunkId(new ObjectId()).index(1).content("Turn order: clockwise...").createdAt(Instant.now()).updatedAt(rb.getUpdatedAt()).build());
            
            when(rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId)).thenReturn(text);

            // Act
            RulebookTextResponseDto dto = rulebookService.getRulebookText(rulebookId);

            // Assert
            Assertions.assertThat(dto.getRulebookId()).isEqualTo(rulebookId.toHexString());
            Assertions.assertThat(dto.getUpdatedAt()).isEqualTo(rb.getUpdatedAt());
            Assertions.assertThat(dto.getLockHeldBy()).isEmpty();
            Mockito.verifyNoInteractions(userRepository);
        }
        
        @Test
        public void testGetRulebookTextResolvesLockHolderUserNameWhenLockHeld(){
            // Arrange
            ObjectId lockHolderId = new ObjectId();
            rb.setLockHeldBy(lockHolderId);
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            List<RulebookText> text = List.of();
            when(rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId)).thenReturn(text);
            
            User user = userWithUsername(lockHolderId.toHexString(),"bob");
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            // Act
            RulebookTextResponseDto dto = rulebookService.getRulebookText(rulebookId);

            // Assert
            Assertions.assertThat(dto.getLockHeldBy()).isEqualTo(user.getUsername());
        }
        
        @Test
        public void testGetRulebookTextThrowsWhenLockHolderUserMissing(){
            // Arrange
            ObjectId lockHolderId = new ObjectId();
            rb.setLockHeldBy(lockHolderId);
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            List<RulebookText> text = List.of();
            when(rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId)).thenReturn(text);
            when(userRepository.findById(lockHolderId.toHexString())).thenReturn(Optional.empty());

            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getRulebookText(rulebookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User does not exist.");
        }
        
        @Test
        public void testGetRulebookTextMapsChunksThroughUnaltered(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            List<RulebookText> text = List.of(RulebookText.builder().rulebookId(rulebookId).chunkId(new ObjectId()).index(0).content("Setup: place the board...").build(),
            RulebookText.builder().rulebookId(rulebookId).chunkId(new ObjectId()).index(1).content("Turn order: clockwise...").build());

            when(rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId)).thenReturn(text);

            // Act
            RulebookTextResponseDto dto = rulebookService.getRulebookText(rulebookId);

            // Assert
            Assertions.assertThat(dto.getChunks()).hasSize(2);
            Assertions.assertThat(dto.getChunks().get(0).getChunkId()).isEqualTo(text.get(0).getChunkId());
            Assertions.assertThat(dto.getChunks().get(0).getIndex()).isEqualTo(text.get(0).getIndex());
            Assertions.assertThat(dto.getChunks().get(0).getContent()).isEqualTo(text.get(0).getContent());
            Assertions.assertThat(dto.getChunks().get(1).getChunkId()).isEqualTo(text.get(1).getChunkId());
            Assertions.assertThat(dto.getChunks().get(1).getIndex()).isEqualTo(text.get(1).getIndex());
            Assertions.assertThat(dto.getChunks().get(1).getContent()).isEqualTo(text.get(1).getContent());
            Assertions.assertThat(dto.getChunks()).extracting(ChunkDto::getIndex).containsExactly(0, 1);
        }
        
        @Test
        public void testGetRulebookTextHandlesEmptyChunkList(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            List<RulebookText> text = List.of();
            when(rulebookTextRepository.findByRulebookIdOrderByIndexAsc(rulebookId)).thenReturn(text);

            // Act
            RulebookTextResponseDto dto = rulebookService.getRulebookText(rulebookId);

            // Assert
            Assertions.assertThat(dto.getChunks()).isEmpty();
        }
    }

    // ---------- AC-VLT-09: Get Rulebook Edit History ----------
    @Nested
    class GetEditHistoryTests{
        private ObjectId rulebookId = null;
        private Rulebook rb = null;

        @BeforeEach
        void setUp() {
            rb = validRulebook();
            rulebookId = rb.getId();
        }

        private EditEvent editEvent(ObjectId editorId, EditType type, String prevContent, String newContent){
            return EditEvent.builder()
                .id(new ObjectId())
                .rulebookId(rulebookId)
                .editorId(editorId)
                .chunkId(new ObjectId())
                .index(0)
                .chunkBefore(null)
                .editType(type)
                .previousContent(prevContent)
                .newContent(newContent)
                .versionPostEdit(5L)
                .compensatesVersion(null)
                .committedAt(Instant.now())
                .build();
        }

        @Test
        public void testGetEditHistoryThrowsWhenRulebookNotFound(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.getEditHistory(rulebookId))
                .isInstanceOf(RulebookNotFoundException.class);

            Mockito.verifyNoInteractions(editEventRepository);
        }
        
        @Test
        public void testGetEditHistoryReturnsEmptyHistoryWhenNoEditsRecorded(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));
            when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId)).thenReturn(List.of());

            // Act
            EditHistoryResponseDto dto = rulebookService.getEditHistory(rulebookId);

            // Assert
            Assertions.assertThat(dto.getRulebookId()).isEqualTo(rulebookId.toHexString());
            Assertions.assertThat(dto.getTotalEdits()).isEqualTo(0);
            Assertions.assertThat(dto.getEdits()).isEmpty();
        }
        
        @Test
        public void testGetEditHistoryMapsMultipleEditsPreservingRepositoryOrder(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            ObjectId editorId = new ObjectId();
            User editor = userWithUsername(editorId.toHexString(), "dave");
            when(userRepository.findById(editorId.toHexString()))
                    .thenReturn(Optional.of(editor));

            EditEvent e1 = editEvent(editorId, EditType.INSERT, null, "Older content");
            EditEvent e2 = editEvent(editorId, EditType.INSERT, "Older content", "Newer content");
            when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId)).thenReturn(List.of(e1, e2));

            // Act
            EditHistoryResponseDto dto = rulebookService.getEditHistory(rulebookId);

            // Assert
            Assertions.assertThat(dto.getTotalEdits()).isEqualTo(2);
            Assertions.assertThat(dto.getEdits()).extracting(EditEventResponseDto::getNewContent)
                .containsExactly("Older content", "Newer content");
        }
        
        @Test
        public void testGetEditHistoryFallsBackToDeletedUserWhenEditorMissing(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            ObjectId editorId = new ObjectId();
            when(userRepository.findById(editorId.toHexString()))
                    .thenReturn(Optional.empty());

            EditEvent event = editEvent(editorId, EditType.INSERT, "old", "new");
            when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId)).thenReturn(List.of(event));

            // Act
            EditHistoryResponseDto dto = rulebookService.getEditHistory(rulebookId);

            // Assert
            Assertions.assertThat(dto.getEdits().get(0).getEditor()).isEqualTo("Deleted User");
        }
        
        @Test
        public void testGetEditHistoryMapsAllFieldsOnEditEventResponse(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            ObjectId editorId = new ObjectId();
            User editor = userWithUsername(editorId.toHexString(), "dave");
            when(userRepository.findById(editorId.toHexString()))
                    .thenReturn(Optional.of(editor));

            EditEvent event = editEvent(editorId, EditType.INSERT, "old content", "new content");
            when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId)).thenReturn(List.of(event));

            // Act
            EditHistoryResponseDto dto = rulebookService.getEditHistory(rulebookId);
            EditEventResponseDto eventDto = dto.getEdits().get(0);

            // Assert
            Assertions.assertThat(eventDto.getId()).isEqualTo(event.getId().toHexString());
            Assertions.assertThat(eventDto.getRulebookId()).isEqualTo(event.getRulebookId().toHexString());
            Assertions.assertThat(eventDto.getEditor()).isEqualTo(editor.getUsername());
            Assertions.assertThat(eventDto.getChunkId()).isEqualTo(event.getChunkId().toHexString());
            Assertions.assertThat(eventDto.getEditType()).isEqualTo(event.getEditType().toString());
            Assertions.assertThat(eventDto.getPreviousContent()).isEqualTo(event.getPreviousContent());
            Assertions.assertThat(eventDto.getNewContent()).isEqualTo(event.getNewContent());
            Assertions.assertThat(eventDto.getVersionPostEdit()).isEqualTo(event.getVersionPostEdit());
            Assertions.assertThat(eventDto.getCommittedAt()).isEqualTo(event.getCommittedAt());
        }
        
        @Test
        public void testGetEditHistoryMapsEditTypeUsingToStringNotEnumName(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            ObjectId editorId = new ObjectId();
            User editor = userWithUsername(editorId.toHexString(), "dave");
            when(userRepository.findById(editorId.toHexString()))
                    .thenReturn(Optional.of(editor));

            EditEvent event = editEvent(editorId, EditType.DELETE, "old", null);
            when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId)).thenReturn(List.of(event));

            // Act
            EditHistoryResponseDto dto = rulebookService.getEditHistory(rulebookId);

            // Assert
            Assertions.assertThat(dto.getEdits().get(0).getEditType()).isEqualTo("DELETE");
        }
        
        @Test
        public void testGetEditHistoryInsertEventHasNullPreviousContent(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            ObjectId editorId = new ObjectId();
            when(userRepository.findById(editorId.toHexString())).thenReturn(Optional.of(userWithUsername(editorId.toHexString(), "dave")));

            EditEvent event = editEvent(editorId, EditType.INSERT, null, "new content");
            when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId)).thenReturn(List.of(event));

            // Act
            EditHistoryResponseDto dto = rulebookService.getEditHistory(rulebookId);

            // Assert
            Assertions.assertThat(dto.getEdits().get(0).getPreviousContent()).isNull();
            Assertions.assertThat(dto.getEdits().get(0).getNewContent()).isEqualTo("new content");
        }
        
        @Test
        public void testGetEditHistoryDeleteEventHasNullNewContent(){
            // Arrange
            when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(rb));

            ObjectId editorId = new ObjectId();
            when(userRepository.findById(editorId.toHexString())).thenReturn(Optional.of(userWithUsername(editorId.toHexString(), "dave")));

            EditEvent event = editEvent(editorId, EditType.DELETE, "old content", null);
            when(editEventRepository.findByRulebookIdOrderByCommittedAtAsc(rulebookId)).thenReturn(List.of(event));

            // Act
            EditHistoryResponseDto dto = rulebookService.getEditHistory(rulebookId);

            // Assert
            Assertions.assertThat(dto.getEdits().get(0).getNewContent()).isNull();
            Assertions.assertThat(dto.getEdits().get(0).getPreviousContent()).isEqualTo("old content");
        }
    }
}