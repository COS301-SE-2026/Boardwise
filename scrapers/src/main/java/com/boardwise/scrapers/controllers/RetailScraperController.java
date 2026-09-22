package com.boardwise.scrapers.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.scrapers.dtos.BoardgamesRequest;
import com.boardwise.scrapers.dtos.RetailSourceItemDTO;
import com.boardwise.scrapers.dtos.ScrapeResultsDTO;
import com.boardwise.scrapers.enums.MatchType;
import com.boardwise.scrapers.services.listing_scraper.RetailScraper;
import com.boardwise.scrapers.services.utils.TitleRelevanceFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/internal/retail")
public class RetailScraperController {
    private final Logger logger = Logger.getLogger(RetailScraperController.class.getName());
    private final RetailScraper retailScraper;

    private static final double RELATED_THRESHOLD = 0.15;
    private static final int MAX_RELATED_RESULTS = 10;

    RetailScraperController(RetailScraper retailScraper){
        this.retailScraper = retailScraper;
    }

    @PostMapping("listings")
    public ResponseEntity<ScrapeResultsDTO> getPersonalisedListings(@RequestBody BoardgamesRequest boardgames) {
        List<String> gameNames = boardgames.boardgames();
        if (gameNames.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, List<RetailSourceItemDTO>> res = new HashMap<>();
        for (String game : gameNames) {
            res.put(game, findListingsForGame(game));
        }
        ScrapeResultsDTO toRet  = new ScrapeResultsDTO(res, gameNames);

        return ResponseEntity.ok(toRet);
    }

    private List<RetailSourceItemDTO> findListingsForGame(String game) {
        try {
            List<RetailSourceItemDTO> scraped = retailScraper.scrapeForBoardgame(game);

            List<RetailSourceItemDTO> exact = scraped.stream()
                .filter(item -> TitleRelevanceFilter.isExactMatch(game, item.retailTitle()))
                .map(item -> item.withMatchType(MatchType.EXACT))
                .toList();

            if (!exact.isEmpty()) {
                return exact;
            }

            return scraped.stream()
                .map(item -> Map.entry(item, TitleRelevanceFilter.relatedScoreUsingJaccaradSimilarity(game, item.retailTitle())))
                .filter(entry -> entry.getValue() >= RELATED_THRESHOLD)
                .sorted(Map.Entry.<RetailSourceItemDTO, Double>comparingByValue().reversed())
                .limit(MAX_RELATED_RESULTS)
                .map(entry -> entry.getKey().withMatchType(MatchType.RELATED))
                .toList();
        } catch (Exception e) {
            logger.log(Level.WARNING, "failed to scrape " + game, e);
            return List.of();
        }
    }

}