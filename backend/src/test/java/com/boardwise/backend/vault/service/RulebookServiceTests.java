package com.boardwise.backend.vault.service;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.vault.dto.response.RulebookSummaryResponseDto;
import com.boardwise.backend.vault.exception.BoardgameNotFoundException;
import com.boardwise.backend.vault.exception.InvalidPaginationException;
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
        Rulebook rb = null;

        @BeforeEach
        void setupSRT(){
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

        /*
         * {
         * "content": [],
         * "empty": true,
         * "first": true,
         * "last": true,
         * "number": 0,
         * "numberOfElements": 0,
         * "pageable": {
         * "offset": 0,
         * "pageNumber": 0,
         * "pageSize": 20,
         * "paged": true,
         * "sort": {
         * "empty": false,
         * "sorted": true,
         * "unsorted": false
         * },
         * "unpaged": false
         * },
         * "size": 20,
         * "sort": {
         * "empty": false,
         * "sorted": true,
         * "unsorted": false
         * },
         * "totalElements": 0,
         * "totalPages": 0
         * }
         */
    }
    // ---------- AC-VLT-03: Get Rulebook Detail ----------
    // @Nested
    // class GetRulebookByIdTests{

    //     @Test
    //     public void testForValidId(){
    //         Instant now1 = Instant.now();
    //         Rulebook mockRulebook1 = Rulebook.builder()
    //                 .id(new ObjectId())
    //                 .gameId(new ObjectId())
    //                 .title("mockGame1")
    //                 .edition("1st Edition")
    //                 .status("Ready")
    //                 .version(1)
    //                 .contributorId(new ObjectId())
    //                 .r2PdfKey("key-1")
    //                 .uploadedAt(now1)
    //                 .updatedAt(now1)
    //                 .build();
    //         // Arrange
    //         // Act
    //         // Assert
    //     }
        
    //     @Test
    //     public void testForLockHeldByPopulatedForExistingWriteLock(){
    //         // Arrange
    //         // Act
    //         // Assert
    //     }

    //     @Test
    //     public void testForLockHeldByIsNullWhenNoWriteLockExists(){
    //         // Arrange
    //         // Act
    //         // Assert
    //     }
    // }

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
