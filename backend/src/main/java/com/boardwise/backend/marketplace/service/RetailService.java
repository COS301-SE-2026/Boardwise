package com.boardwise.backend.marketplace.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.shared.model.Boardgame;
import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.models.Preferences;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.UserRepository;


@Service
public class RetailService {
    //Logger
    private static final Logger logger = Logger.getLogger(RetailService.class.getName());
    private static final int PAGESIZE = 20;

    //AUTH
    private final JWTService jwtService;

    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;
     
    //REST CLIENT
    private final RestClient scraperClient;

    public RetailService(UserRepository userRepository, BoardGameRepository boardGameRepository, JWTService jwtService, @Qualifier("scraperRestClient") RestClient scraperclient) {
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
        this.jwtService = jwtService;
        this.scraperClient = scraperclient;
    }

    private Page<RetailSourceItemDTO> paginate(List<RetailSourceItemDTO> overall, Integer pageNum) {
        int page = (pageNum == null || pageNum < 0) ? 0 : pageNum;
        Pageable pageable = PageRequest.of(page, PAGESIZE);
 
        int start = (int) pageable.getOffset();
        if (start >= overall.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, overall.size());
        }
        int end = Math.min(start + pageable.getPageSize(), overall.size());
        return new PageImpl<>(overall.subList(start, end), pageable, overall.size());
    }

    private Page<RetailSourceItemDTO> emptyPage(Integer pageNum) {
        int page = (pageNum == null || pageNum < 0) ? 0 : pageNum;
        return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, PAGESIZE), 0);
    }

    private final int NUM_SUGGESTED_GAMES= 5;
    private int getNumGamesToScrapeFor(List<String> ls){
        int numGamesToSearchFor = NUM_SUGGESTED_GAMES - ls.size();

        if(numGamesToSearchFor < 0){
            numGamesToSearchFor = ls.size() - NUM_SUGGESTED_GAMES;
        }
        else if(numGamesToSearchFor == NUM_SUGGESTED_GAMES){
            numGamesToSearchFor = NUM_SUGGESTED_GAMES;
        }
        return numGamesToSearchFor;
    }

    private Boardgame getRandomBoardGameByGenre(String genre, List<String> suggBoardgames){
        List<Boardgame> games = boardGameRepository.findByGenresIn(genre)
            .stream()
            .filter((g)-> !suggBoardgames.contains(g.getId()))
            .limit(5)
            .toList();

            if(games.isEmpty()){
                throw new IllegalArgumentException ("Games with genre " + genre + " doesn't exist in db");
            }

            Random r = new Random(12345);
            logger.info("value of random: " + r);
            int index = r.nextInt(0, games.size());
            return games.get(index);
    }

    private List<String> buildGamePrefrenceList(String token){
        ObjectId userId = jwtService.extractUserId(token);

        User user = userRepository.findById(userId.toString()).orElseThrow();

        List<String> suggBoardgames = new ArrayList<>(user.getOwnedGames());

        if (suggBoardgames.size() < NUM_SUGGESTED_GAMES) {
            Preferences userPreferences = user.getPreferences();
            List<String> genreList = userPreferences.getGenres();

            if (!genreList.isEmpty()) {
                int numGamesToSearchFor = getNumGamesToScrapeFor(genreList);

                for (String genre : genreList) {
                    if (numGamesToSearchFor == 0) break;

                    try {
                        suggBoardgames.add(getRandomBoardGameByGenre(genre, suggBoardgames).getId());
                        numGamesToSearchFor--;
                    } catch (IllegalArgumentException e) {
                        logger.warning("No games available for genre " + genre + ", skipping");
                    }
                }
            }
        }

        return suggBoardgames;
    }

    public HashMap<String, List<RetailSourceItemDTO>> getPersonalisedRetailListings(String token, Integer page) {
        List<String> gameIds = buildGamePrefrenceList(token.replace("Bearer ", ""));
        if (gameIds.isEmpty()) {
            return new HashMap<>();
        }

        List<String> titles = boardGameRepository.findAllById(gameIds)
            .stream()
            .map(Boardgame::getTitle)
            .toList();

        Map<String, List<RetailSourceItemDTO>> allListings = fetchListingsFromScraper(titles);

        List<Map.Entry<String, RetailSourceItemDTO>> flattened = new ArrayList<>();
        for (Map.Entry<String, List<RetailSourceItemDTO>> entry : allListings.entrySet()) {
            for (RetailSourceItemDTO item : entry.getValue()) {
                flattened.add(Map.entry(entry.getKey(), item));
            }
        }

        int pageNum = (page == null || page < 0) ? 0 : page;
        int start = pageNum * PAGESIZE;
        if (start >= flattened.size()) {
            return new HashMap<>();
        }
        int end = Math.min(start + PAGESIZE, flattened.size());

        HashMap<String, List<RetailSourceItemDTO>> result = new HashMap<>();
        for (Map.Entry<String, RetailSourceItemDTO> entry : flattened.subList(start, end)) {
            result.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
        }
        return result;
    }

    private Map<String, List<RetailSourceItemDTO>> fetchListingsFromScraper(List<String> boardgameTitles) {
        try {
            return scraperClient.get()
                .uri(uriBuilder -> {
                    var b = uriBuilder.path("/internal/retail/listings");
                    boardgameTitles.forEach(title -> b.queryParam("boardgames", title));
                    return b.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, List<RetailSourceItemDTO>>>() {});
        } catch (RestClientException e) {
            logger.warning("Failed to fetch personalized listings: " + e.getMessage());
            return Map.of();
        }
    }

}