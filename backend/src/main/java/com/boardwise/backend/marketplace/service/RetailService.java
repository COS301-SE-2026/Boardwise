package com.boardwise.backend.marketplace.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.model.ScrapeCache;
import com.boardwise.backend.marketplace.repository.ScrapeCacheRepository;
import com.boardwise.backend.marketplace.service.webscrapers.BobShopScraper;
import com.boardwise.backend.marketplace.service.webscrapers.TakealotScraper;
import com.boardwise.backend.marketplace.service.webscrapers.ToysRUsScraper;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.user_service.repos.UserRepository.GameOwnershipCount;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.repos.BoardGameRepository;


@Service
public class RetailService {

    private static final int PAGE_SIZE = 20;

    private final TakealotScraper ts;
    private final BobShopScraper bss;
    private final ToysRUsScraper trus;

    private final ScrapeCacheRepository scrapeCacheRepository;
    
    @Value("${scrape.cache.ttl.minutes:60}")
    private long ttlMin;

    public RetailService(ScrapeCacheRepository scrapeCacheRepository,TakealotScraper ts, BobShopScraper bss, ToysRUsScraper trus, UserRepository userRepository, BoardGameRepository boardGameRepository) {
        this.ts = ts;
        this.bss = bss;
        this.trus = trus;
        this.scrapeCacheRepository = scrapeCacheRepository;
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
    }

    protected List<RetailSourceItemDTO> findWebListings(String s) {
        if (!StringUtils.hasText(s)) {
            return new ArrayList<>();
        }

        // individual processes happening concurrently
        CompletableFuture<List<RetailSourceItemDTO>> takealotFuture = CompletableFuture.supplyAsync(() -> safeScrape(ts::scrape, s, "Takealot"));
        CompletableFuture<List<RetailSourceItemDTO>> bobShopFuture = CompletableFuture.supplyAsync(() -> safeScrape(bss::scrape, s, "BobShop"));
        CompletableFuture<List<RetailSourceItemDTO>> toysRUsFuture = CompletableFuture.supplyAsync(() -> safeScrape(trus::scrape, s, "ToysRUs"));

        CompletableFuture.allOf(takealotFuture, bobShopFuture, toysRUsFuture).join();

        List<RetailSourceItemDTO> overall = new ArrayList<>();
        overall.addAll(takealotFuture.join());
        overall.addAll(bobShopFuture.join());
        overall.addAll(toysRUsFuture.join());

        return overall;
    }

    protected List<RetailSourceItemDTO> findWebListingsCached(String s) {
        if (!StringUtils.hasText(s)) {
            return new ArrayList<>();
        }

        return scrapeCacheRepository.findBySearchTerm(s)
            .filter(this::isFresh)
            .map(cache -> {
                System.out.println("Cache hit for s=[" + s + "]");
                return cache.getResults();
            })
            .orElseGet(() -> rescrapeAndCache(s));
    }

    private boolean isFresh(ScrapeCache cache) {
        if (cache.getLastScrapedAt() == null) return false;
        long ageMinutes = java.time.Duration.between(cache.getLastScrapedAt(), java.time.LocalDateTime.now()).toMinutes();
        return ageMinutes < ttlMin;
    }

    private List<RetailSourceItemDTO> safeScrape(Function<String, List<RetailSourceItemDTO>> scraper,
            String query, String sourceName) {
        try {
            List<RetailSourceItemDTO> result = scraper.apply(query);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Page<RetailSourceItemDTO> getRetailListingsPage(String s, Integer pageNum) {
        List<RetailSourceItemDTO> overall = findWebListingsCached(s);

        int page = (pageNum == null || pageNum < 0) ? 0 : pageNum;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        int start = (int) pageable.getOffset();
        if (start >= overall.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, overall.size());
        }
        int end = Math.min(start + pageable.getPageSize(), overall.size());

        return new PageImpl<>(overall.subList(start, end), pageable, overall.size());
    }

    private List<RetailSourceItemDTO> rescrapeAndCache(String s) {
        System.out.println("Cache miss/stale, rescraping for s=[" + s + "]");
        List<RetailSourceItemDTO> overall = findWebListings(s); 

        ScrapeCache existing = scrapeCacheRepository.findBySearchTerm(s).orElse(null);

        ScrapeCache toSave =ScrapeCache.builder()
        .id(existing != null ? existing.getId() : null)
        .searchTerm(s)
        .results(overall)
        .lastScrapedAt(LocalDateTime.now())
        .build();

        scrapeCacheRepository.save(toSave);
        return overall;
    }

    //RECCOMMENDATION ALGORITHM
    //CURRENT APPROACH: fetch as many listings as possible 
    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;
     
    //number of Games to search for 
    private final int NUMOFGAMES = 3;
    @Scheduled(fixedDelayString = "${scrape.cache.refresh.interval.ms:3600000}")
    protected void recommendedScraper(){
        //SHOULDDO: compute most popular first 
        //get first n games 
        List<GameOwnershipCount> currGames = new ArrayList<>(userRepository.findMostOwnedGameIds(NUMOFGAMES));
        //fall back 
        List<Boardgame> games = new ArrayList<>();

        if(currGames.size() < 3){
           games = boardGameRepository.findAllBy(Limit.of(NUMOFGAMES));

           if(games.isEmpty()){
            throw new RuntimeException("Error while trying to fetch popular games");
           }

        } else{
            for(GameOwnershipCount x : currGames){
                // get Game from boardgame 

                Optional<Boardgame> bg = boardGameRepository.findById(x.getId());

                if(bg.isEmpty()){
                    throw new RuntimeException("Error while trying to fetch id: " + x.getId());
                }
                
                //bg is not empty/ is PRESENT
                games.add(bg.get());
            }
        }
        //scrape based on current 

        for(Boardgame bg : games){
            this.findWebListingsCached(bg.getTitle()+" Boardgame");
        }
    }
}