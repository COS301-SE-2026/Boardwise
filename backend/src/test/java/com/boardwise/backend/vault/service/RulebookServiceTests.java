package com.boardwise.backend.vault.service;

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
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.vault.dto.response.RulebookSummaryResponseDto;
import com.boardwise.backend.vault.exception.BoardgameNotFoundException;
import com.boardwise.backend.vault.exception.InvalidPaginationException;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;

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

    @InjectMocks
    private RulebookService rulebookService;

    @BeforeEach
    void setUpServiceFields(){
        ReflectionTestUtils.setField(rulebookService, "r2PublicDomain", "https://test-cdn.example.com");
        ReflectionTestUtils.setField(rulebookService, "rulebooksBucket", "test-bucket");
    }
    
    // ---------- AC-VLT-02: List/ Search Rulebooks ----------
    @Nested
    class ListOrSearchRulebook{
        final long totalRulebooks = 25;
        List<Rulebook> mockRulebooks = new ArrayList<>();
        List<Boardgame> mockBoardgames = new ArrayList<>();
        Pageable pageable = null;
        final int page = 1;
        final int limit = 20;

        @BeforeEach
        void setup(){
            for (int i = 0; i < totalRulebooks; i++) {
                Instant now = Instant.now();
                ObjectId gameId = new ObjectId();
                List<String> genres = new ArrayList<>();
                genres.add("This " + i);
                genres.add("That " + i);
                genres.add("The Third " + i);
                mockBoardgames.add(Boardgame.builder()
                        .id(gameId.toHexString())
                        .title("mockGame" + i)
                        .description("mock description " + i)
                        .imageURL("mock-image-url-" + i)
                        .genres(genres)
                        .build());
                mockRulebooks.add(Rulebook.builder()
                        .id(new ObjectId())
                        .gameId(gameId)
                        .title("mockGame" + i)
                        .edition("Edition " + i)
                        .status("Ready")
                        .version(i)
                        .contributorId(new ObjectId())
                        .r2PdfKey("key-" + i)
                        .uploadedAt(now)
                        .updatedAt(now)
                        .build());
            }
            pageable = PageRequest.of(page-1, Math.min(limit, 100), Sort.by(Sort.Direction.DESC, "updatedAt"));
        }

        @Test
        public void testThatSearchRulebooksWithNoParamsReturnsOrderedFirstPageOf20(){
            // Arrange
            List<Rulebook> firstTwenty = mockRulebooks.subList(0, 20);
            for (Rulebook rb : firstTwenty) {
                Boardgame bg = mockBoardgames.stream().filter(b -> b.getId().equals(rb.getGameId().toHexString())).findFirst().orElseThrow();
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(firstTwenty, pageable, totalRulebooks);
            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("",1,20);

            // Assert
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(rulebookRepository).findByStatusAndTitleContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq(""), pageableCaptor.capture());
            
            Assertions.assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
            Assertions.assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);

            Assertions.assertThat(result.getContent().size()).isEqualTo(20);
        }
        
        @Test
        public void testForPartialMatchReturingMatchingRulebooks(){
            // Arrange
            List<Rulebook> filteredRulebooks = mockRulebooks.stream()
                .filter(rb -> rb.getTitle().contains("Game1"))
                .collect(Collectors.toList());
                
            List<Boardgame> filteredBoardgames = mockBoardgames.stream()
                .filter(bg -> bg.getTitle().contains("Game1"))
                .collect(Collectors.toList());
            for (Boardgame bg : filteredBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(filteredRulebooks, pageable, filteredRulebooks.size());
            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq("Game1"), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("Game1", 1, 20);

            // Assert
            Assertions.assertThat(result.getContent().size()).isEqualTo(11);
            for (RulebookSummaryResponseDto rb : result.getContent()) {
                Assertions.assertThat(rb.getTitle().contains("Game1")).isTrue();
            }
        }

        @Test
        public void testForSearchCaseInsensitivity(){
            // Arrange
            List<Rulebook> filteredRulebooks = mockRulebooks.stream()
                    .filter(rb -> rb.getTitle().contains("Game1"))
                    .collect(Collectors.toList());

            List<Boardgame> filteredBoardgames = mockBoardgames.stream()
                    .filter(bg -> bg.getTitle().contains("Game1"))
                    .collect(Collectors.toList());
            for (Boardgame bg : filteredBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(filteredRulebooks, pageable, filteredRulebooks.size());
            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq("game1"), Mockito.any(Pageable.class)))
                    .thenReturn(page);

            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("game1", 1, 20);

            // Assert
            Assertions.assertThat(result.getContent().size()).isEqualTo(11);
            for (RulebookSummaryResponseDto rb : result.getContent()) {
                Assertions.assertThat(rb.getTitle().contains("Game1")).isTrue();
            }
        }

        @Test
        public void testForSearchWithZeroMatches(){
            // Arrange
            List<Rulebook> filteredRulebooks = mockRulebooks.stream()
                    .filter(rb -> rb.getTitle().contains("GameNotInTheList"))
                    .collect(Collectors.toList());

            List<Boardgame> filteredBoardgames = mockBoardgames.stream()
                    .filter(bg -> bg.getTitle().contains("GameNotInTheList"))
                    .collect(Collectors.toList());
            for (Boardgame bg : filteredBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(filteredRulebooks, pageable, filteredRulebooks.size());
            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq("GameNotInTheList"), Mockito.any(Pageable.class)))
                    .thenReturn(page);

            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("GameNotInTheList", 1, 20);

            // Assert
            Assertions.assertThat(result.getContent().size()).isEqualTo(0);
            Assertions.assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        public void testForPageLimitCapEnforcement(){
            // Arrange
            List<Rulebook> mockRulebooksLimitTest = new ArrayList<>();
            List<Boardgame> mockBoardgamesLimitTest = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Instant now = Instant.now();
                ObjectId gameId = new ObjectId();
                mockBoardgamesLimitTest.add(Boardgame.builder()
                        .id(gameId.toHexString())
                        .title("mockGame" + i)
                        .genres(List.of("Genre" + i))
                        .build());
                mockRulebooksLimitTest.add(Rulebook.builder()
                        .id(new ObjectId())
                        .gameId(gameId)
                        .title("mockGame" + i)
                        .edition("Edition " + i)
                        .status("Ready")
                        .version(i)
                        .contributorId(new ObjectId())
                        .r2PdfKey("key-" + i)
                        .uploadedAt(now)
                        .updatedAt(now)
                        .build());
            }
            for (Boardgame bg : mockBoardgamesLimitTest) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenAnswer( inv -> {
                        Pageable requestedPageable = inv.getArgument(2);
                        return new PageImpl<>(mockRulebooksLimitTest, requestedPageable, 200);
                    });
            // Act
            rulebookService.searchRulebooks("", 1, 200);
            
            // Assert
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(rulebookRepository).findByStatusAndTitleContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq(""), pageableCaptor.capture());

            Assertions.assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        public void testForOrderedByUpdatedAt(){
            // Arrange
            List<Rulebook> firstTwenty = mockRulebooks.subList(0, 20);
            for (Rulebook rb : firstTwenty) {
                Boardgame bg = mockBoardgames.stream().filter(b -> b.getId().equals(rb.getGameId().toHexString()))
                        .findFirst().orElseThrow();
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(firstTwenty, pageable, totalRulebooks);
            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("", 1, 20);

            // Assert
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(rulebookRepository).findByStatusAndTitleContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq(""), pageableCaptor.capture());
            
            Pageable capturedPageable = pageableCaptor.getValue();

            Assertions.assertThat(capturedPageable.getSort().getOrderFor("updatedAt")).isNotNull();
            Assertions.assertThat(capturedPageable.getSort().getOrderFor("updatedAt").isDescending()).isTrue();

            int idx= 0;
            for (Rulebook rb : page.getContent()) {
                Assertions.assertThat(rb.getTitle().compareTo(result.getContent().get(idx++).getTitle()) == 0).isTrue();
            }
        }

        @Test
        public void testForPageOffsetConversionBeingCorrect(){ // the 1-indexed must be translated to 0-indexed
            // Arrange
            int offsetPage = 4; // Should translate to page 3
            int offsetPageSize = 5;
            for (Boardgame bg : mockBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenAnswer(inv -> {
                        Pageable requestedPageable = inv.getArgument(2);
                        return new PageImpl<>(mockRulebooks, requestedPageable, 200);
                    });
            // Act
            rulebookService.searchRulebooks("", offsetPage, offsetPageSize);
            
            // Assert
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(rulebookRepository).findByStatusAndTitleContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq(""), pageableCaptor.capture());

            Assertions.assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(offsetPage-1);
            Assertions.assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(offsetPageSize);
        }

        @Test
        public void testForNullGameIdCase(){
            // Arrange
            Page<Rulebook> page = new PageImpl<>(List.of(Rulebook.builder().id(new ObjectId()).r2CoverKey("key-1").build()), pageable, 1);
            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("",1, 20);
            
            // Assert
            Assertions.assertThat(result.getContent().get(0).getGenres()).isEmpty();
            Assertions.assertThat(result.getContent().get(0).getMinPlayers()).isEqualTo(-1);
            Assertions.assertThat(result.getContent().get(0).getMaxPlayers()).isEqualTo(-1);
            Assertions.assertThat(result.getContent().get(0).getMinAge()).isEqualTo(-1);
            Assertions.assertThat(result.getContent().get(0).getDuration()).isEqualTo(-1);
            Mockito.verifyNoInteractions(boardgameRepository);

        }

        @Test
        public void testBoardgameLookupCallCount(){
            // Arrange
            List<Rulebook> firstTwenty = mockRulebooks.subList(0, 20);
            for (Rulebook rb : firstTwenty) {
                Boardgame bg = mockBoardgames.stream().filter(b -> b.getId().equals(rb.getGameId().toHexString()))
                        .findFirst().orElseThrow();
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(firstTwenty, pageable, totalRulebooks);
            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("", 1, 20);

            // Assert
            for(Rulebook rb: firstTwenty){
                Mockito.verify(boardgameRepository, Mockito.times(1)).findById(rb.getGameId().toHexString());
            }
            
            Mockito.verify(boardgameRepository, Mockito.times(20)).findById(Mockito.anyString());
            Assertions.assertThat(result.getContent().size()).isEqualTo(20);
        }

        @Test
        public void testThatMissingBoardgameThrowsBoardgameNotFoundException(){
            // Arrange
            Rulebook rulebook = mockRulebooks.get(0);
            Mockito.when(boardgameRepository.findById(rulebook.getGameId().toHexString())).thenReturn(Optional.empty());

            Page<Rulebook> page = new PageImpl<>(List.of(rulebook), pageable, 1);
            Mockito.when(rulebookRepository.findByStatusAndTitleContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class))).thenReturn(page);
            
            // Act and Assert
            Assertions.assertThatThrownBy(() -> rulebookService.searchRulebooks("", 1, 20)).isInstanceOf(BoardgameNotFoundException.class);
        }

        @Nested
        class TestLimitBoundaryValues{
            @Test
            public void testLimitZeroThrowsInvalidPaginationException(){
                // Act and Assert
                Assertions.assertThatThrownBy(() -> rulebookService.searchRulebooks("", 1, 0))
                .isInstanceOf(InvalidPaginationException.class);
                Mockito.verifyNoInteractions(rulebookRepository, boardgameRepository);
            }

            @Test
            public void testLimitNegativeThrowsInvalidPaginationException() {
                // Act and Assert
                Assertions.assertThatThrownBy(() -> rulebookService.searchRulebooks("", 1, -5))
                    .isInstanceOf(InvalidPaginationException.class);
                Mockito.verifyNoInteractions(rulebookRepository, boardgameRepository);
            }

            @Test
            public void testPageZeroThrowsInvalidPaginationException(){
                // Act and Assert
                Assertions.assertThatThrownBy(() -> rulebookService.searchRulebooks("", 0, 20)).isInstanceOf(InvalidPaginationException.class);
                Mockito.verifyNoInteractions(rulebookRepository, boardgameRepository);
            }

            @Test
            public void testPageNegativeThrowsInvalidPaginationException() {
                // Act and Assert
                Assertions.assertThatThrownBy(() -> rulebookService.searchRulebooks("", -1, 20)).isInstanceOf(InvalidPaginationException.class);
                Mockito.verifyNoInteractions(rulebookRepository, boardgameRepository);
            }
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
    // class GetRulebookDetail{

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
    // ---------- AC-VLT-05: Get Rulebook Text State ----------
    // ---------- AC-VLT-09: Get Rulebook Edit History ----------
}
