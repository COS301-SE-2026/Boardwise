package com.boardwise.backend.user_service.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.repos.BoardGameRepository;

@SpringBootTest
@Testcontainers
@DisplayName("Board Game Service Integration Tests")
public class BoardGameServiceIntegrationTests {

    @Container
    private static MongoDBContainer container = new MongoDBContainer("mongo:latest");

    @Autowired
    private BoardGameRepository gameRepo;

    @Autowired
    private BoardGameService service;


    @BeforeEach
    void setUp(){
        gameRepo.deleteAll();
    }   

    @Test
    @DisplayName("Call BGG API and Populate BOARD_GAMES collection.")
    void shouldCallBGGAPIandPopulateDatabase(){
        // Act (no arrange here is no mocking)
        service.populateDatabase();

        // assert
        List<Boardgame> savedGames = gameRepo.findAll();
        Boardgame first = savedGames.get(0);
        assertTrue(savedGames.size() <= 20);
        assertTrue(first.getBggId() == 1);
        assertTrue(first.getTitle().equals("Die Macher"));
        assertTrue(first.getDescription().contains("different regions of Germany"));
        assertTrue(first.getImageURL().equals("https://cf.geekdo-images.com/rpwCZAjYLD940NWwP3SRoA__original/img/yR0aoBVKNrAmmCuBeSzQnMflLYg=/0x0/filters:format(jpeg)/pic4718279.jpg"));
        assertTrue(first.getGenres().equals(List.of("Economic", "Negotiation", "Political")));
        assertTrue(first.getMinPlayers() == 3);
        assertTrue(first.getMaxPlayers() == 5);
    }
}
