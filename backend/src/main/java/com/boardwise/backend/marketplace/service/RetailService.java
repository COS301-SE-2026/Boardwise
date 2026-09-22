package com.boardwise.backend.marketplace.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.boardwise.backend.marketplace.dtos.retailsource.BoardgamesRequest;
import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.dtos.retailsource.ScrapeResultsDTO;
import com.boardwise.backend.shared.model.Boardgame;
import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.models.user_preferences.*;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.boardwise.backend.user_service.repository.UserRepository.GameOwnershipCount;

@Service
public class RetailService {
    private static final Logger logger = Logger.getLogger(RetailService.class.getName());
    private static final int PAGESIZE = 20;
    private static final int NUM_SUGGESTED_GAMES = 10;
    private static final int NUM_FALLBACK_GAMES = 5;

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;
    private final RestClient scraperClient;

    public RetailService(UserRepository userRepository,
                         BoardGameRepository boardGameRepository,
                         JWTService jwtService,
                         @Qualifier("scraperRestClient") RestClient scraperclient) {
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
        this.jwtService = jwtService;
        this.scraperClient = scraperclient;
    }

    private Boardgame getRandomBoardGameByGenre(String genre, List<String> suggBoardgames) {
        List<Boardgame> games = boardGameRepository.findByGenresIn(genre)
                .stream()
                .filter((g) -> !suggBoardgames.contains(g.getId()))
                .limit(5)
                .toList();

        if (games.isEmpty()) {
            throw new IllegalArgumentException("Games with genre " + genre + " doesn't exist in db");
        }

        Random r = new Random(12345);
        logger.info("value of random: " + r);
        int index = r.nextInt(0, games.size());
        return games.get(index);
    }

    private List<String> buildGamePrefrenceList(String token) {
        ObjectId userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId.toString()).orElseThrow();

        List<String> ownedGameIds = new ArrayList<>(user.getOwnedGames());

        if (ownedGameIds.isEmpty()) {
            return getTopOwnedGameIds(NUM_FALLBACK_GAMES);
        }

        if (ownedGameIds.size() >= NUM_SUGGESTED_GAMES) {
            return ownedGameIds;
        }

        List<String> genreList = resolveGenres(user, ownedGameIds);

        int numGamesToSearchFor = NUM_SUGGESTED_GAMES - ownedGameIds.size();

        for (String genre : genreList) {
            if (numGamesToSearchFor == 0) break;

            try {
                ownedGameIds.add(getRandomBoardGameByGenre(genre, ownedGameIds).getId());
                numGamesToSearchFor--;
            } catch (IllegalArgumentException e) {
                logger.warning("No games available for genre " + genre + ", skipping");
            }
        }

        return ownedGameIds;
    }

    private List<String> resolveGenres(User user, List<String> ownedGameIds) {
        Preferences prefs = user.getPreferences();
        List<String> preferred = (prefs != null && prefs.getGenres() != null)
                ? prefs.getGenres().stream().filter(g -> g != null && !g.isBlank()).toList()
                : List.of();

        if (!preferred.isEmpty()) {
            return preferred;
        }

        List<String> derived = boardGameRepository.findAllById(ownedGameIds)
                .stream()
                .filter(bg -> bg.getGenres() != null)
                .flatMap(bg -> bg.getGenres().stream())
                .filter(g -> g != null && !g.isBlank())
                .distinct()
                .toList();

        if (derived.isEmpty()) {
            logger.warning("User " + user.getId()
                    + " has owned games but no resolvable genres; falling back to popular games");
            return List.of();
        }
        return derived;
    }

    private List<String> getTopOwnedGameIds(int limit) {
        return userRepository.findMostOwnedGameIds(limit)
                .stream()
                .map(GameOwnershipCount::getId)
                .toList();
    }

    public Page<RetailSourceItemDTO> getPersonalisedRetailListings(String token, Integer page) {
        int pageNum = (page == null || page < 0) ? 0 : page;
        Pageable pageable = PageRequest.of(pageNum, PAGESIZE);

        List<String> gameIds = buildGamePrefrenceList(token.replace("Bearer ", ""));
        if (gameIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<String> titles = boardGameRepository.findAllById(gameIds)
                .stream()
                .map(Boardgame::getTitle)
                .limit(NUM_SUGGESTED_GAMES)
                .toList();

        return fetchListingsFromScraper(titles, pageNum, PAGESIZE);
    }

    private Page<RetailSourceItemDTO> fetchListingsFromScraper(List<String> titles, int page, int size) {
        try {
            ScrapeResultsDTO response = scraperClient.post()
                    .uri("/listings")
                    .body(new BoardgamesRequest(titles, page, size))
                    .retrieve()
                    .body(ScrapeResultsDTO.class);

            if (response == null) {
                return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
            }

            Pageable pageable = PageRequest.of(response.number(), response.size());

            return new PageImpl<>(
                    response.content() == null ? List.of() : response.content(),
                    pageable,
                    response.totalElements()
            );
        } catch (RestClientException e) {
            logger.warning("Failed to fetch personalized listings: " + e.getMessage());
            return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
        }
    }
}