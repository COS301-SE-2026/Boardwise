package com.boardwise.scrapers.services.listing_scraper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service 
public class RetailScraper {
    private final BgbsaScraper bgbsaScraper;
    private final LevelUpStoreScraper levelUpStoreScraper;
    private final TableTopGuruScraper tableTopGuruScraper;
    private final TimelessBoardGamesScraper timelessBoardGamesScraper;

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>(500);

    public RetailScraper(BgbsaScraper bgbsaScraper, LevelUpStoreScraper levelUpStoreScraper, TableTopGuruScraper tableTopGuruScraper,TimelessBoardGamesScraper timelessBoardGamesScraper){
        this.bgbsaScraper = bgbsaScraper;
        this.levelUpStoreScraper = levelUpStoreScraper;
        this.tableTopGuruScraper = tableTopGuruScraper;
        this.timelessBoardGamesScraper = timelessBoardGamesScraper;
    }

}
