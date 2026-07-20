package com.boardwise.backend.user_service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import com.boardwise.backend.shared.services.BoardGameService;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.BoardGameSearch;

@ExtendWith(MockitoExtension.class)
@DisplayName("Board Game Service Tests")
public class BoardGameServiceUnitTests {

    private BoardGameRepository gameRepo;
    private BoardGameSearch gameSearch;
    private R2StorageService bucket;
    private MockRestServiceServer mockServer;
    private BoardGameService service;
    private String baseUrl = "https://boardgamegeek.com/xmlapi2";


    @BeforeEach
    void setUp(){
        gameRepo = mock(BoardGameRepository.class);
        bucket = mock(R2StorageService.class);
        gameSearch = mock(BoardGameSearch.class);

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient testClient = builder.baseUrl(baseUrl)
                                        .defaultHeader("Authorization", "Bearer some-valid-token")
                                        .build();

        service = new BoardGameService(gameRepo, bucket, testClient, gameSearch);
    }

    @Test
    @DisplayName("Pulls from BGG and populates DB starting at BGG ID 1")
    void shouldPopulateBoardGameCollectionWithGamesStartingAt1(){
        // Arrange
        when(gameRepo.findTopByBggIdNotNullOrderByBggIdDesc())
            .thenReturn(Optional.empty());

        String requestUrl = baseUrl + "/thing?id=1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20&subtype=boardgame";
        String mockResponse = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item type="boardgame" id="1">
                    <thumbnail>https://cf.geekdo-images.com/3EmD1SEI5fVpR4rbdkU0AA__small/img/pic882119.jpg</thumbnail>
                    <image>https://cf.geekdo-images.com/3EmD1SEI5fVpR4rbdkU0AA__original/img/pic882119.jpg</image>
                    <name type="primary" sortindex="1" value="1830: Railways &amp; Robber Barons" />
                    <description>1830 is one of the most famous 18xx games. Extremely vicious, robber baron oriented stock market.</description>
                    <yearpublished value="1986" />
                    <minplayers value="2" />
                    <maxplayers value="7" />
                    <playingtime value="360" />
                    <minplaytime value="180" />
                    <maxplaytime value="360" />
                    <minage value="14" />
                    <link type="boardgamecategory" id="1021" value="Economic" />
                    <link type="boardgamemechanic" id="2005" value="Stock Holding" />
                    <link type="boardgamedesigner" id="58" value="Francis Tresham" />
                    <link type="boardgamepublisher" id="234" value="Lookout Games" />
                </item>
                <item type="boardgame" id="2">
                    <thumbnail>https://cf.geekdo-images.com/b5VyYjNfAxJ4Z-Dx2UWlqg__small/img/pic7945692.jpg</thumbnail>
                    <image>https://cf.geekdo-images.com/b5VyYjNfAxJ4Z-Dx2UWlqg__original/img/pic7945692.jpg</image>
                    <name type="primary" sortindex="5" value="The Quest for El Dorado" />
                    <description>In The Quest for El Dorado, players take the roles of expedition leaders who have embarked on a search for the legendary land of gold.</description>
                    <yearpublished value="2017" />
                    <minplayers value="2" />
                    <maxplayers value="4" />
                    <playingtime value="60" />
                    <minplaytime value="30" />
                    <maxplaytime value="60" />
                    <minage value="10" />
                    <link type="boardgamecategory" id="1031" value="Racing" />
                    <link type="boardgamemechanic" id="2664" value="Deck, Bag, and Pool Building" />
                    <link type="boardgamedesigner" id="2" value="Reiner Knizia" />
                    <link type="boardgamepublisher" id="34" value="Ravensburger AG" />
                </item>
            </items>
            """;
        
        mockServer.expect(MockRestRequestMatchers.requestTo(requestUrl))
                .andRespond(MockRestResponseCreators.withSuccess(
                    mockResponse,
                    MediaType.APPLICATION_XML
                ));

        // Act
        service.populateDatabase();

        // Assert
        mockServer.verify();
        ArgumentCaptor<List<Boardgame>> captor = ArgumentCaptor.forClass(List.class);
        verify(gameRepo).saveAll(captor.capture());
        List<Boardgame> argument = captor.getValue();
        assertEquals(argument.size(), 2);
        assertEquals(argument.get(0).getBggId(), 1);
        assertEquals(argument.get(0).getTitle(), "1830: Railways & Robber Barons");
        assertTrue(argument.get(0).getDescription().contains("Extremely vicious"));
        assertEquals(argument.get(0).getImageURL(), "https://cf.geekdo-images.com/3EmD1SEI5fVpR4rbdkU0AA__original/img/pic882119.jpg");
        assertEquals(argument.get(0).getGenres(), List.of("Economic"));
        assertTrue(argument.get(0).getMinPlayers() == 2);
        assertTrue(argument.get(0).getMaxPlayers() == 7);
        assertEquals(argument.get(0).getDuration(), 360);
        assertEquals(argument.get(0).getMinAge(), 14);
    }
    
    @Test
    @DisplayName("Pulls from BGG and populates DB starting at last known BGG ID")
    void shouldPopulateBoardGameCollectionWithGamesStartingAtLastKnownBggId(){
        // Arrange
        when(gameRepo.findTopByBggIdNotNullOrderByBggIdDesc())
            .thenReturn(Optional.of(new Boardgame(
                null,
                420,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )));

        String requestUrl = baseUrl + "/thing?id=421,422,423,424,425,426,427,428,429,430,431,432,433,434,435,436,437,438,439,440&subtype=boardgame";
        String mockResponse = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item type="boardgame" id="421">
                    <thumbnail>https://cf.geekdo-images.com/3EmD1SEI5fVpR4rbdkU0AA__small/img/pic882119.jpg</thumbnail>
                    <image>https://cf.geekdo-images.com/3EmD1SEI5fVpR4rbdkU0AA__original/img/pic882119.jpg</image>
                    <name type="primary" sortindex="1" value="1830: Railways &amp; Robber Barons" />
                    <description>1830 is one of the most famous 18xx games. Extremely vicious, robber baron oriented stock market.</description>
                    <yearpublished value="1986" />
                    <minplayers value="2" />
                    <maxplayers value="7" />
                    <playingtime value="360" />
                    <minplaytime value="180" />
                    <maxplaytime value="360" />
                    <minage value="14" />
                    <link type="boardgamecategory" id="1021" value="Economic" />
                    <link type="boardgamemechanic" id="2005" value="Stock Holding" />
                    <link type="boardgamedesigner" id="58" value="Francis Tresham" />
                    <link type="boardgamepublisher" id="234" value="Lookout Games" />
                </item>
                <item type="boardgame" id="422">
                    <thumbnail>https://cf.geekdo-images.com/b5VyYjNfAxJ4Z-Dx2UWlqg__small/img/pic7945692.jpg</thumbnail>
                    <image>https://cf.geekdo-images.com/b5VyYjNfAxJ4Z-Dx2UWlqg__original/img/pic7945692.jpg</image>
                    <name type="primary" sortindex="5" value="The Quest for El Dorado" />
                    <description>In The Quest for El Dorado, players take the roles of expedition leaders who have embarked on a search for the legendary land of gold.</description>
                    <yearpublished value="2017" />
                    <minplayers value="2" />
                    <maxplayers value="4" />
                    <playingtime value="60" />
                    <minplaytime value="30" />
                    <maxplaytime value="60" />
                    <minage value="10" />
                    <link type="boardgamecategory" id="1031" value="Racing" />
                    <link type="boardgamemechanic" id="2664" value="Deck, Bag, and Pool Building" />
                    <link type="boardgamedesigner" id="2" value="Reiner Knizia" />
                    <link type="boardgamepublisher" id="34" value="Ravensburger AG" />
                </item>
            </items>
            """;
        
        mockServer.expect(MockRestRequestMatchers.requestTo(requestUrl))
                .andRespond(MockRestResponseCreators.withSuccess(
                    mockResponse,
                    MediaType.APPLICATION_XML
                ));

        // Act
        service.populateDatabase();

        // Assert
        mockServer.verify();
        ArgumentCaptor<List<Boardgame>> captor = ArgumentCaptor.forClass(List.class);
        verify(gameRepo).saveAll(captor.capture());
        List<Boardgame> argument = captor.getValue();
        assertEquals(argument.size(), 2);
        assertEquals(argument.get(0).getBggId(), 421);
        assertEquals(argument.get(0).getTitle(), "1830: Railways & Robber Barons");
        assertTrue(argument.get(0).getDescription().contains("Extremely vicious"));
        assertEquals(argument.get(0).getImageURL(), "https://cf.geekdo-images.com/3EmD1SEI5fVpR4rbdkU0AA__original/img/pic882119.jpg");
        assertEquals(argument.get(0).getGenres(), List.of("Economic"));
        assertTrue(argument.get(0).getMinPlayers() == 2);
        assertTrue(argument.get(0).getMaxPlayers() == 7);
        assertEquals(argument.get(0).getDuration(), 360);
        assertEquals(argument.get(0).getMinAge(), 14);
    }
}
