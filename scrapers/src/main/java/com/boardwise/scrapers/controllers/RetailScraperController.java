package com.boardwise.scrapers.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.scrapers.dtos.BoardgamesRequest;
import com.boardwise.scrapers.dtos.RetailSourceItemDTO;
import com.boardwise.scrapers.dtos.ScrapeResultsDTO;
import com.boardwise.scrapers.enums.MatchType;
import com.boardwise.scrapers.services.listing_scraper.RetailScraper;
import com.boardwise.scrapers.services.utils.TitleRelevanceFilter;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.annotation.PreDestroy;


@RestController
@RequestMapping("/internal/retail")
public class RetailScraperController {
    private final Logger logger = Logger.getLogger(RetailScraperController.class.getName());
    private final RetailScraper retailScraper;

    private static final double RELATED_THRESHOLD = 0.15;
    private static final int MAX_RELATED_RESULTS = 10;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    RetailScraperController(RetailScraper retailScraper){
        this.retailScraper = retailScraper;
    }

    @PreDestroy
    void shutdown(){
        executor.shutdown();
    }

    @PostMapping("listings")
    public ResponseEntity<ScrapeResultsDTO> getPersonalisedListings(@RequestBody BoardgamesRequest boardgames) {
        List<String> gameNames = boardgames.titles();
        if (gameNames == null || gameNames.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        int page = Math.max(0, boardgames.page());
        int size = boardgames.size() <= 0 ? DEFAULT_PAGE_SIZE : boardgames.size();

        Map<String, CompletableFuture<List<RetailSourceItemDTO>>> futures = new LinkedHashMap<>();
        for (String game : gameNames) {
            futures.put(game, CompletableFuture.supplyAsync(() -> findListingsForGame(game), executor));
        }

        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();

        List<RetailSourceItemDTO> all = futures.values().stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .sorted(Comparator
                        .comparing(RetailSourceItemDTO::retailTitle,
                                Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(RetailSourceItemDTO::price,
                                Comparator.nullsLast(Double::compareTo))
                        .thenComparing(RetailSourceItemDTO::url,
                                Comparator.nullsLast(String::compareTo)))
                .toList();

        long totalElements = all.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<RetailSourceItemDTO> content = all.subList(from, to);

        ScrapeResultsDTO toRet = new ScrapeResultsDTO(
                content,
                page,
                size,
                totalElements,
                totalPages,
                (long) (page + 1) * size >= totalElements,
                page == 0,
                content.size(),
                content.isEmpty()
        );

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