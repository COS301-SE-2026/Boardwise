package com.boardwise.scrapers.services.listing_scraper;

import com.boardwise.scrapers.repositories.ScrapeCacheRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import java.util.function.Supplier;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.boardwise.scrapers.dtos.BgbsaDTO;
import com.boardwise.scrapers.dtos.LevelUpStoreResDTO;
import com.boardwise.scrapers.dtos.RetailSourceItemDTO;
import com.boardwise.scrapers.dtos.TableTopGuruResDTO;
import com.boardwise.scrapers.dtos.TimelessBoardGamesResDTO;
import com.boardwise.scrapers.models.Boardgame;
import com.boardwise.scrapers.models.ScrapeCache;
import com.boardwise.scrapers.repositories.BoardGameRepository;
import com.boardwise.scrapers.repositories.UserRepository;
import com.boardwise.scrapers.repositories.UserRepository.GameOwnershipCount;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import jakarta.annotation.PreDestroy;

@Service 
public class RetailScraper {
    private final ScrapeCacheRepository scrapeCacheRepository;

    private static final Logger logger = Logger.getLogger(RetailScraper.class.getName());

    private static final Duration TTL = Duration.ofHours(1);
    private static final Duration EMPTY_TTL = Duration.ofMinutes(5);
    private static final long SCRAPER_TIMEOUT_SECONDS = 30;
    private final ConcurrentHashMap<String, ReentrantLock> termLocks = new ConcurrentHashMap<>();
    private static final int NUM_OF_GAMES = 50;
    

    private final ExecutorService scheduledExecutor = Executors.newFixedThreadPool(2);
    private final ExecutorService recommendedRunnerExecutor = Executors.newSingleThreadExecutor();


    private final BgbsaScraper bgbsaScraper;
    private final LevelUpStoreScraper levelUpStoreScraper;
    private final TableTopGuruScraper tableTopGuruScraper;
    private final TimelessBoardGamesScraper timelessBoardGamesScraper;
    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;

    private final AsyncCache<String, List<RetailSourceItemDTO>> cache = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfter(new ResultExpiry())
        .buildAsync();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    

    public RetailScraper(BgbsaScraper bgbsaScraper, LevelUpStoreScraper levelUpStoreScraper, TableTopGuruScraper tableTopGuruScraper,TimelessBoardGamesScraper timelessBoardGamesScraper, UserRepository userRepository, BoardGameRepository boardGameRepository, ScrapeCacheRepository scrapeCacheRepository){
        this.bgbsaScraper = bgbsaScraper;
        this.levelUpStoreScraper = levelUpStoreScraper;
        this.tableTopGuruScraper = tableTopGuruScraper;
        this.timelessBoardGamesScraper = timelessBoardGamesScraper;
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
        this.scrapeCacheRepository = scrapeCacheRepository;
    }

    @Scheduled(fixedDelayString = "${scrape.cache.refresh.interval.ms:3600000}", initialDelayString = "${scrape.cache.refresh.interval.ms:3600000}")
    public void scheduledRecommendedScraper(){
        runRecommendedScraper();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(){
        recommendedRunnerExecutor.submit(this::runRecommendedScraper);
    }
    
    
    private void runRecommendedScraper(){
        List<GameOwnershipCount> topOwned = userRepository.findMostOwnedGameIds(NUM_OF_GAMES);
        List<Boardgame> games = new ArrayList<>();

        if(topOwned.size() < 5){
            games = getGloballyPopularGames(NUM_OF_GAMES);
        }
        else{
            for(GameOwnershipCount x: topOwned){
                boardGameRepository.findById(x.getId())
                    .ifPresentOrElse(games::add, ()-> logger.warning("Skipping missing boardgame id during scheduled scrape: " + x.getId())
                    );
            }
        }

        if(games.isEmpty()){
            logger.warning("No boardgames available to scrape (empty owned and fallback lists)");
            return;
        }

        for (Boardgame bg : games) {
            logger.info(() -> "Scraping for: " + bg.getTitle() + " boardgames");
            long start = System.currentTimeMillis();
            scheduledExecutor.submit(() -> scrapeForBoardgame(bg.getTitle() + " Boardgame"));
            long took = System.currentTimeMillis() - start;
            logger.info(() -> "Dispatched after: " + took + " ms");
        }
    }
    
    private String normalise(String boardgame){
        return boardgame.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    public Map<String, List<RetailSourceItemDTO>> scrapeForBoardgames(List<String> boardgames) {
        Map<String, CompletableFuture<List<RetailSourceItemDTO>>> futures = new LinkedHashMap<>();

        for (String b : boardgames) {
            if (b == null || b.isBlank()) continue;
            String key = normalise(b);
            futures.computeIfAbsent(b, ignored ->
                cache.get(key, (k, e) -> loadFromMongoOrScrape(k))
                    .exceptionally(ex ->{
                        logger.log(Level.WARNING, "Failed to load listings for " + b, ex);
                        return List.of();
                    }));
        }

        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();

        Map<String, List<RetailSourceItemDTO>> results = new LinkedHashMap<>();
        futures.forEach((game, future) -> results.put(game, future.join()));
        return results;
    }


    private List<Boardgame> getGloballyPopularGames(int count) {
        List<GameOwnershipCount> topOwned = userRepository.findMostOwnedGameIds(count);
        List<Boardgame> games = new ArrayList<>();

        if (!topOwned.isEmpty()) {
            List<String> ownedIds = topOwned.stream().map(GameOwnershipCount::getId).toList();
            games.addAll(boardGameRepository.findAllById(ownedIds));
        }

        int shortfall = count - games.size();
        if (shortfall > 0) {
            List<String> alreadySelectedIds = games.stream().map(Boardgame::getId).toList();
            boardGameRepository.findAllBy(Limit.of(shortfall + count)).stream()
                .filter(bg -> !alreadySelectedIds.contains(bg.getId()))
                .limit(shortfall)
                .forEach(games::add);
        }
        return games.size() > count ? games.subList(0, count) : games;
    }

    public List<RetailSourceItemDTO> scrapeForBoardgame(String boardgame){
        if(boardgame == null || boardgame.isBlank()){
            return List.of();
        }

        String key = normalise(boardgame);

        return cache.get(key, (k,e) -> loadFromMongoOrScrape(k)).join();
    }

    private CompletableFuture<List<RetailSourceItemDTO>> scrapeAll(String term){
        var bgbsa = supply("BGBSA", ()-> bgbsaScraper.scrapeForBoardgame(term));
        var levelUp = supply("LevelUp", ()-> levelUpStoreScraper.scrapeForBoardgame(term));
        var guru = supply("TableTopGuru", ()-> tableTopGuruScraper.scrapeForBoardgame(term));
        var timeless = supply("TimelessBoardGames", ()-> timelessBoardGamesScraper.scrapeForBoardgame(term));

        
        return CompletableFuture.allOf(bgbsa, levelUp, guru, timeless)
            .thenApply(v ->Stream.of(   
                bgbsa.join().stream().map(this::fromBgbsa),
                levelUp.join().stream().map(this::fromLevelUp),
                guru.join().stream().filter(TableTopGuruResDTO::isAvailable).map(this::fromGuru),
                timeless.join().stream().filter(t -> Boolean.TRUE.equals(t.isAvailable())).map(this::fromTimeless))
            .flatMap(Function.identity())
            .toList());
    }

    private RetailSourceItemDTO fromBgbsa(BgbsaDTO data){
        return RetailSourceItemDTO.builder()
        .retailTitle(data.retailerTitle())
        .retailer(data.retailer())
        .url(data.url())
        .price(effectivePrice(data.isOnSale(), data.originalPrice(), data.salePrice()))
        .imageUrl(data.imageUrl())
        .build();
    }

    private RetailSourceItemDTO fromLevelUp(LevelUpStoreResDTO data){
        return RetailSourceItemDTO.builder()
        .retailTitle(data.retailerTitle())
        .retailer(data.retailer())
        .url(data.url())
        .price(effectivePrice(data.isOnSale(), data.originalPrice(), data.salePrice()))
        .imageUrl(data.imageUrl())
        .build();
    }

    private RetailSourceItemDTO fromGuru(TableTopGuruResDTO data) {
        return RetailSourceItemDTO.builder()
            .retailTitle(data.retailTitle())
            .retailer(data.retailer())
            .url(data.url())
            .price(effectivePrice(data.isOnSale(), data.originalPrice(), data.salePrice()))
            .imageUrl(data.imageUrl())
            .build();
    }

    private RetailSourceItemDTO fromTimeless(TimelessBoardGamesResDTO data) {
        return RetailSourceItemDTO.builder()
            .retailTitle(data.retailTitle())
            .retailer(data.retailer())
            .url(data.url())
            .price(effectivePrice(data.isOnSale(), data.originalPrice(), data.salePrice()))
            .imageUrl(data.imageUrl())
            .build();
    }
    
    private <T> CompletableFuture<List<T>> supply(String name, Supplier<List<T>> task){
        return CompletableFuture.supplyAsync(task,executor)
        .orTimeout(SCRAPER_TIMEOUT_SECONDS, TimeUnit.SECONDS)   
        .exceptionally(e ->{
            logger.log(Level.WARNING, name + " scraper failed", e);
            return List.of();
        });
    }

    private Double effectivePrice(Boolean isOnSale, Double originalPrice, Double salePrice) {// if On sale, return on sale price 
        return Boolean.TRUE.equals(isOnSale) && salePrice != null ? salePrice : originalPrice;
    }

    private static final class ResultExpiry implements Expiry<String, List<RetailSourceItemDTO>>{
        @Override 
        public long expireAfterCreate(String key,  List<RetailSourceItemDTO> value, long currentTime){
            return((value.isEmpty())? EMPTY_TTL: TTL).toNanos();
        }

        @Override 
        public long expireAfterUpdate(String key, List<RetailSourceItemDTO> value, long currentTime, long currentDuration){
            return currentDuration;
        }

        @Override 
        public long expireAfterRead(String key,  List<RetailSourceItemDTO> value, long currentTime, long currentDuration){
            return currentDuration;
        }
    }
    
    private CompletableFuture<List<RetailSourceItemDTO>> loadFromMongoOrScrape(String boardgame){
        Optional<ScrapeCache> res = scrapeCacheRepository.findBySearchTerm(boardgame);

        if (res.isPresent() && isFresh(res.get())) {
            return CompletableFuture.completedFuture(res.get().getResults());
        }

        return CompletableFuture.supplyAsync(() -> {
            ReentrantLock lock = termLocks.computeIfAbsent(boardgame, k -> new ReentrantLock());
            lock.lock();
            try {
                Optional<ScrapeCache> recheck = scrapeCacheRepository.findBySearchTerm(boardgame);
                if (recheck.isPresent() && isFresh(recheck.get())) {
                    return recheck.get().getResults();
                }

                List<RetailSourceItemDTO> results = scrapeAll(boardgame).join();

                ScrapeCache existing = recheck.orElse(null);
                ScrapeCache toSave = ScrapeCache.builder()
                    .id(existing != null ? existing.getId() : null)
                    .searchTerm(boardgame)
                    .results(results)
                    .lastScrapedAt(java.time.LocalDateTime.now())
                    .build();

                scrapeCacheRepository.save(toSave);
                return results;
            } finally {
                lock.unlock();
                termLocks.remove(boardgame, lock);
            }
        }, executor);
    }
    
    private boolean isFresh(ScrapeCache cache) {
        if (cache.getLastScrapedAt() == null) return false;
        long ageSeconds = java.time.Duration.between(cache.getLastScrapedAt(), java.time.LocalDateTime.now()).getSeconds();
        return ageSeconds < TTL.toSeconds();
    }

    public void prewarmListings(String gameTitle) {
        if (gameTitle == null || gameTitle.isBlank()) return;
        scheduledExecutor.submit(() -> scrapeForBoardgame(gameTitle + " Boardgame"));
    }
    
    @PreDestroy 
    void shutdown(){
        executor.shutdown();
        scheduledExecutor.shutdown();
        recommendedRunnerExecutor.shutdown();
    }
}
