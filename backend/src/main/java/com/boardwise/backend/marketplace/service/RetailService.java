package com.boardwise.backend.marketplace.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
import com.boardwise.backend.shared.dtos.GameListDTO;
import com.boardwise.backend.shared.dtos.GenreRequestDTO;
import com.boardwise.backend.shared.dtos.OnboardingDTO;
import com.boardwise.backend.shared.model.Boardgame;
import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.BoardGameService;
import com.boardwise.backend.user_service.models.user_preferences.*;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.boardwise.backend.user_service.repository.UserRepository.GameOwnershipCount;

@Service
public class RetailService {
    private static final Logger logger = Logger.getLogger(RetailService.class.getName());
    private static final int PAGESIZE = 20;
    private static final int NUM_SUGGESTED_GAMES = 10;

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;
    private final BoardGameService boardGameService;
    private final RestClient scraperClient;

    public RetailService(UserRepository userRepository,
                         BoardGameRepository boardGameRepository,
                         JWTService jwtService,
                         @Qualifier("scraperRestClient") RestClient scraperclient, BoardGameService boardGameService) {
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
        this.jwtService = jwtService;
        this.scraperClient = scraperclient;
        this.boardGameService = boardGameService;
    }

    private List<String> buildGamePrefrenceList(String token) {
        ObjectId userId;
        try {
            userId = jwtService.extractUserId(token);
        } catch (Exception e) {
            logger.warning("buildGamePrefrenceList: extractUserId failed: " + e.getMessage());
            return List.of();
        }

        User user;
        try {
            user = userRepository.findById(userId.toString())
                    .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        } catch (Exception e) {
            logger.warning("buildGamePrefrenceList: user lookup failed: " + e.getMessage());
            return List.of();
        }

        List<String> ownedGames = user.getOwnedGames();
        List<String> ownedGameIds = new ArrayList<>(ownedGames == null ? List.of() : ownedGames);

        if (ownedGameIds.isEmpty()) {
            try {
                Object response = boardGameService.getBoardgames(null, null);
                if (response instanceof Map<?, ?> map) {
                    Object raw = map.get("boardGames");
                    if (raw instanceof List<?> list) {
                        for (Object item : list) {
                            if (ownedGameIds.size() >= NUM_SUGGESTED_GAMES) break;
                            if (item instanceof GameListDTO dto && dto.id() != null) {
                                ownedGameIds.add(dto.id());
                            }
                        }
                    } else {
                        logger.warning("buildGamePrefrenceList: 'boardgames' key missing or not a List");
                    }
                } else {
                    logger.warning("buildGamePrefrenceList: getBoardgames returned null or non-map");
                }
            } catch (Exception e) {
                logger.warning("buildGamePrefrenceList: getBoardgames failed: " + e.getMessage());
            }
            return ownedGameIds;
        }

        if (ownedGameIds.size() >= NUM_SUGGESTED_GAMES) {
            return ownedGameIds.stream().limit(NUM_SUGGESTED_GAMES).toList();
        }

        Set<String> seen = new HashSet<>(ownedGameIds);

        List<String> genreList;
        try {
            genreList = resolveGenres(user, ownedGameIds);
        } catch (Exception e) {
            logger.warning("buildGamePrefrenceList: resolveGenres failed: " + e.getMessage());
            genreList = List.of();
        }

        for (String genre : genreList) {
            if (ownedGameIds.size() >= NUM_SUGGESTED_GAMES) break;

            try {
                List<OnboardingDTO> suggestions = boardGameService
                        .getPopularGamesBasedOnGenres(new GenreRequestDTO(List.of(genre)), 1);

                if (suggestions == null || suggestions.isEmpty()) {
                    logger.warning("No games available for genre " + genre + ", skipping");
                    continue;
                }

                for (OnboardingDTO suggestion : suggestions) {
                    if (ownedGameIds.size() >= NUM_SUGGESTED_GAMES) break;
                    if (suggestion == null || suggestion.id() == null) continue;
                    if (seen.add(suggestion.id())) {
                        ownedGameIds.add(suggestion.id());
                    }
                }
            } catch (Exception e) {
                logger.warning("No games available for genre " + genre + ", skipping: " + e.getMessage());
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


    public Page<RetailSourceItemDTO> getPersonalisedRetailListings(String token, Integer page) {
        int pageNum = (page == null || page < 0) ? 0 : page;
        Pageable pageable = PageRequest.of(pageNum, PAGESIZE);

        String cleanedToken = token == null ? "" : token.replace("Bearer ", "");
        System.out.println("getPersonalisedRetailListings: entered, token length=" + cleanedToken.length());

        List<String> gameIds = buildGamePrefrenceList(cleanedToken);
        System.out.println("PREFERENCE LIST OF: " + gameIds);

        if (gameIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<String> titles = boardGameRepository.findAllById(gameIds)
                .stream()
                .map(Boardgame::getTitle)
                .limit(NUM_SUGGESTED_GAMES)
                .toList();

        System.out.println("TITLES: " + titles);

        return fetchListingsFromScraper(titles, pageNum, PAGESIZE);
    }

    public Page<RetailSourceItemDTO> fetchListingsFromScraper(List<String> titles, int page, int size) {
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