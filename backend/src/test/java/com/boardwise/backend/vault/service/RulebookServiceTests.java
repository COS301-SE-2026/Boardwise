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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.vault.dto.response.RulebookSummaryResponseDto;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;
import com.boardwise.backend.vault.repository.WriteLockRepository;

@ExtendWith(MockitoExtension.class)
public class RulebookServiceTests {
    @Mock
    private RulebookRepository rulebookRepository;

    @Mock
    private RulebookTextRepository rulebookTextRepository;

    @Mock
    private WriteLockRepository writeLockRepository;

    @Mock
    private EditEventRepository editEventRepository;

    @Mock
    private BoardGameRepository boardgameRepository;

    @InjectMocks
    private RulebookService rulebookService;
    
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
                        .gameName("mockGame" + i)
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
            for (Boardgame bg : mockBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(mockRulebooks, pageable, totalRulebooks);
            Mockito.when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("",1,20);

            // Assert
            Assertions.assertThat(result.getSize()).isEqualTo(20);
        }
        
        @Test
        public void testForPartialMatchReturingMatchingRulebooks(){
            // Arrange
            List<Rulebook> filteredRulebooks = mockRulebooks.stream()
                .filter(rb -> rb.getGameName().contains("Game1"))
                .collect(Collectors.toList());
                
            List<Boardgame> filteredBoardgames = mockBoardgames.stream()
                .filter(bg -> bg.getTitle().contains("Game1"))
                .collect(Collectors.toList());
            for (Boardgame bg : filteredBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(filteredRulebooks, pageable, filteredRulebooks.size());
            Mockito.when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
                Mockito.eq("Ready"), Mockito.eq("Game1"), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("Game1", 1, 20);

            // Assert
            Assertions.assertThat(result.getContent().size()).isEqualTo(11);
            for (RulebookSummaryResponseDto rb : result.getContent()) {
                Assertions.assertThat(rb.getGameName().contains("Game1")).isTrue();
            }
        }

        @Test
        public void testForSearchCaseInsensitivity(){
            // Arrange
            List<Rulebook> filteredRulebooks = mockRulebooks.stream()
                    .filter(rb -> rb.getGameName().contains("Game1"))
                    .collect(Collectors.toList());

            List<Boardgame> filteredBoardgames = mockBoardgames.stream()
                    .filter(bg -> bg.getTitle().contains("Game1"))
                    .collect(Collectors.toList());
            for (Boardgame bg : filteredBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(filteredRulebooks, pageable, filteredRulebooks.size());
            Mockito.when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq("game1"), Mockito.any(Pageable.class)))
                    .thenReturn(page);

            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("game1", 1, 20);

            // Assert
            Assertions.assertThat(result.getContent().size()).isEqualTo(11);
            for (RulebookSummaryResponseDto rb : result.getContent()) {
                Assertions.assertThat(rb.getGameName().contains("Game1")).isTrue();
            }
        }

        @Test
        public void testForSearchWithZeroMatches(){
            // Arrange
            List<Rulebook> filteredRulebooks = mockRulebooks.stream()
                    .filter(rb -> rb.getGameName().contains("GameNotInTheList"))
                    .collect(Collectors.toList());

            List<Boardgame> filteredBoardgames = mockBoardgames.stream()
                    .filter(bg -> bg.getTitle().contains("GameNotInTheList"))
                    .collect(Collectors.toList());
            for (Boardgame bg : filteredBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(filteredRulebooks, pageable, filteredRulebooks.size());
            Mockito.when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
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
            for (int i = 0; i < 200; i++) {
                Instant now = Instant.now();
                ObjectId gameId = new ObjectId();
                List<String> genres = new ArrayList<>();
                genres.add("This " + i);
                genres.add("That " + i);
                genres.add("The Third " + i);
                mockBoardgamesLimitTest.add(Boardgame.builder()
                        .id(gameId.toHexString())
                        .title("mockGame" + i)
                        .description("mock description " + i)
                        .imageURL("mock-image-url-" + i)
                        .genres(genres)
                        .build());
                mockRulebooksLimitTest.add(Rulebook.builder()
                        .id(new ObjectId())
                        .gameId(gameId)
                        .gameName("mockGame" + i)
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
            pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "updatedAt"));
            Page<Rulebook> page = new PageImpl<>(mockRulebooksLimitTest, pageable, 200);
            Mockito.when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("", 1, 200);
            // Assert
            Assertions.assertThat(result.getSize()).isEqualTo(100);
        }

        // @Test
        public void testForOrderedByUpdatedAt(){
            // Arrange
            for (Boardgame bg : mockBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }

            Page<Rulebook> page = new PageImpl<>(mockRulebooks, pageable, totalRulebooks);
            Mockito.when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("", 1, 20);

            // Assert
            Assertions.assertThat(result.getSort().isSorted()).isTrue();
            int idx= 0;
            Instant currentLargerThanNext = Instant.now();
            for (Rulebook rb : page.getContent()) {
                Assertions.assertThat(rb.getGameName().compareTo(result.getContent().get(idx++).getGameName()) == 0).isTrue();
                Assertions.assertThat(currentLargerThanNext.compareTo(rb.getUpdatedAt()) > 0).isTrue();
                currentLargerThanNext = rb.getUpdatedAt();
            }
        }

        @Test
        public void testForPageOffsetConversionBeingCorrect(){ // the 1-indexed must be translated to 0-indexed
            // Arrange
            int offsetPage = 4; // Should translate to page 3
            int offsetPageSize = 5; // Should translate to page 3
            for (Boardgame bg : mockBoardgames) {
                Mockito.when(boardgameRepository.findById(bg.getId())).thenReturn(Optional.of(bg));
            }
            pageable = PageRequest.of(offsetPage - 1, offsetPageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
            Page<Rulebook> page = new PageImpl<>(mockRulebooks, pageable, 200);
            Mockito.when(rulebookRepository.findByStatusAndGameNameContainingIgnoreCase(
                    Mockito.eq("Ready"), Mockito.eq(""), Mockito.any(Pageable.class)))
                    .thenReturn(page);
            // Act
            Page<RulebookSummaryResponseDto> result = rulebookService.searchRulebooks("", offsetPage, offsetPageSize);
            // Assert
            Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(offsetPage-1);
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
    // 2. Pagination
    // 3. Search Edge Cases
    // 4. Authentication
    // 5. Response Shape
    // ---------- AC-VLT-03: Get Rulebook Detail ----------
    Instant now1 = Instant.now();
    Rulebook mockRulebook1 = Rulebook.builder()
            .id(new ObjectId())
            .gameId(new ObjectId())
            .gameName("mockGame1")
            .edition("1st Edition")
            .status("Ready")
            .version(1)
            .contributorId(new ObjectId())
            .r2PdfKey("key-1")
            .uploadedAt(now1)
            .updatedAt(now1)
            .build();
    // ---------- AC-VLT-04: Download Raw PDF ----------
    // ---------- AC-VLT-05: Get Rulebook Text State ----------
    // ---------- AC-VLT-09: Get Rulebook Edit History ----------
}
