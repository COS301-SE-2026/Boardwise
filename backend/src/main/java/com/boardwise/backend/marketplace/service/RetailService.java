package com.boardwise.backend.marketplace.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.models.ScrapeCache;
import com.boardwise.backend.marketplace.repository.ScrapeCacheRepository;
import com.boardwise.backend.marketplace.service.webscrapers.BobShopScraper;
import com.boardwise.backend.marketplace.service.webscrapers.TakealotScraper;
import com.boardwise.backend.marketplace.service.webscrapers.ToysRUsScraper;
import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.boardwise.backend.user_service.repository.UserRepository.GameOwnershipCount;

import jakarta.annotation.PreDestroy;

import com.boardwise.backend.shared.model.Boardgame;
import com.boardwise.backend.user_service.models.User;


@Service
public class RetailService {

    //Logger
    private static final Logger logger = Logger.getLogger(RetailService.class.getName());

    private static final int PAGESIZE = 20;

    private final TakealotScraper ts;
    private final BobShopScraper bss;
    private final ToysRUsScraper trus;

    @Value("${scrape.cache.max.entries:300}")
    private int maxCacheEntries;

    private final Map<String, ScrapeCache> cache = Collections.synchronizedMap(
        new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ScrapeCache> eldest) {
                return size() > maxCacheEntries;
            }
        });
    private final ConcurrentHashMap<String, ReentrantLock> termLocks = new ConcurrentHashMap<>();

    //AUTH
    private final JWTService jwtService;

    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;
     
    //number of Games to search for 
    private final int NUMOFGAMES = 50;

    private final ExecutorService scraperExecutor = Executors.newFixedThreadPool(3); // to execute the 3 scrapers 
    private final ExecutorService scheduledScraperExecutor = Executors.newFixedThreadPool(2); // execute scheduled process
    private final ExecutorService recommendedScraperRunnerExecutor = Executors.newSingleThreadExecutor(); //once game gets added 


    private final ScrapeCacheRepository scrapeCacheRepository;
    
    @Value("${scrape.cache.ttl.seconds:3600}")
    private long ttlSeconds;

    public RetailService(ScrapeCacheRepository scrapeCacheRepository,TakealotScraper ts, BobShopScraper bss, ToysRUsScraper trus, UserRepository userRepository, BoardGameRepository boardGameRepository, JWTService jwtService) {
        this.ts = ts;
        this.bss = bss;
        this.trus = trus;
        this.scrapeCacheRepository = scrapeCacheRepository;
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
        this.jwtService = jwtService;
    }

    private List<RetailSourceItemDTO> safeJoin(CompletableFuture<List<RetailSourceItemDTO>> future) {
    if (future.isDone() && !future.isCompletedExceptionally()) {
        return future.join();
    }
    future.cancel(true); // stops waitinhg
    return new ArrayList<>();
}

    protected List<RetailSourceItemDTO> findWebListings(String s) {
        return findWebListings(s, scraperExecutor);
    }

    private List<RetailSourceItemDTO> findWebListings(String s, ExecutorService executor) {
        if (!StringUtils.hasText(s)) {
            return new ArrayList<>();
        }

        // individual processes happening concurrently
        CompletableFuture<List<RetailSourceItemDTO>> takealotFuture =  CompletableFuture.supplyAsync(() -> safeScrape(ts::scrape, s), executor);
        CompletableFuture<List<RetailSourceItemDTO>> bobShopFuture = CompletableFuture.supplyAsync(() -> safeScrape(bss::scrape, s), executor);
        CompletableFuture<List<RetailSourceItemDTO>> toysRUsFuture = CompletableFuture.supplyAsync(() -> safeScrape(trus::scrape, s), executor);
        
        try {
            CompletableFuture.allOf(takealotFuture, bobShopFuture, toysRUsFuture)
                .orTimeout(40, TimeUnit.SECONDS)
                .join();
        } catch (Exception e) {
            logger.warning("Scrape timed out or failed: " + e.getMessage());
        }
        List<RetailSourceItemDTO> overall = new ArrayList<>();
        overall.addAll(safeJoin(takealotFuture));
        overall.addAll(safeJoin(bobShopFuture));
        overall.addAll(safeJoin(toysRUsFuture));


        return overall;
    }

    protected List<RetailSourceItemDTO> findWebListingsCached(String s) {
        return findWebListingsCached(s, scraperExecutor);
    }

    private List<RetailSourceItemDTO> findWebListingsCached(String s, ExecutorService executor) {
        if (!StringUtils.hasText(s)) {
            return new ArrayList<>();
        }

        ScrapeCache cached = cache.get(s);
        if (cached != null && isFresh(cached)) {
            return cached.getResults();
        }

        ReentrantLock lock = termLocks.computeIfAbsent(s, k -> new ReentrantLock());
        lock.lock();
        try {
            cached = cache.get(s);
            if (cached != null && isFresh(cached)) {
                return cached.getResults();
            }

            return scrapeCacheRepository.findBySearchTerm(s)
                .filter(this::isFresh)
                .map(c -> {
                    cache.put(s, c); // in-memory layer
                    return c.getResults();
                })
                .orElseGet(() -> rescrapeAndCache(s, executor));
        } finally {
            lock.unlock();
            termLocks.remove(s, lock);
        }
    }

    private boolean isFresh(ScrapeCache cache) {
        if (cache.getLastScrapedAt() == null) return false;
        long ageSeconds = java.time.Duration.between(cache.getLastScrapedAt(), java.time.LocalDateTime.now()).getSeconds();
        return ageSeconds < ttlSeconds;
    }

    private List<RetailSourceItemDTO> safeScrape(Function<String, List<RetailSourceItemDTO>> scraper,
            String query) {
        try {
            List<RetailSourceItemDTO> result = scraper.apply(query);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
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
    private static final int NOGAMEFALLBACKCOUNT = 5;

    private List<Boardgame> getGloballyPopularGames() {
        return getGloballyPopularGames(NUMOFGAMES);
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
            List<Boardgame> fallbackCandidates = boardGameRepository.findAllBy(Limit.of(shortfall + count));
 
            fallbackCandidates.stream()
                .filter(bg -> !alreadySelectedIds.contains(bg.getId()))
                .limit(shortfall)
                .forEach(games::add);
        }
        return games.size() > count ? games.subList(0, count) : games;
    }

    private Page<RetailSourceItemDTO> emptyPage(Integer pageNum) {
        int page = (pageNum == null || pageNum < 0) ? 0 : pageNum;
        return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, PAGESIZE), 0);
    }
    
    public Page<RetailSourceItemDTO> getPersonalizedRetailListings(String token, Integer pageNum) {
    ObjectId userId = jwtService.extractUserId(token);
    Optional<User> user = userRepository.findById(userId.toString());

    List<RetailSourceItemDTO> combined = new ArrayList<>();

    if (user.isPresent()) {
        List<String> ownedGameIds = user.get().getOwnedGames();
        List<Boardgame> games;

        if (ownedGameIds != null && !ownedGameIds.isEmpty()) {
            List<String> topNGames = ownedGameIds.stream().limit(10).toList();
            games = boardGameRepository.findAllById(topNGames);
        } else {
            games = getGloballyPopularGames(NOGAMEFALLBACKCOUNT);
        }

        for (Boardgame bg : games) {
            combined.addAll(findWebListingsCached(bg.getTitle() + " Boardgame"));
        }
    } else {
        logger.info(() -> "Error while trying to find user for personalised listings: " + userId.toString());
    }
    //padding for results
    if (combined.isEmpty()) {
        return fallbackToAllCached(pageNum);
    }

    combined = padWithCachedFallback(combined);

    return paginate(combined, pageNum);
}

    private List<RetailSourceItemDTO> padWithCachedFallback(List<RetailSourceItemDTO> existing) {
        int needed = PAGESIZE - existing.size();
        if (needed <= 0) return existing;

        Set<String> seenKeys = existing.stream()
            .map(this::itemKey)
            .collect(Collectors.toSet());

        List<RetailSourceItemDTO> padded = new ArrayList<>(existing);
        int added = 0;

        List<ScrapeCache> recentCaches = scrapeCacheRepository
            .findByLastScrapedAtAfterOrderByLastScrapedAtDesc(
                LocalDateTime.now().minusSeconds(ttlSeconds), Limit.of(50));

        for (ScrapeCache c : recentCaches) {
            if (added >= needed) break;
            for (RetailSourceItemDTO item : c.getResults()) {
                if (added >= needed) break;
                if (seenKeys.add(itemKey(item))) {
                    padded.add(item);
                    added++;
                }
            }
        }

        return padded;
    }

    private String itemKey(RetailSourceItemDTO item) {
        return item.retailer() + "|" + item.url();
    }
        
    private Page<RetailSourceItemDTO> fallbackToAllCached(Integer pageNum) {
        List<ScrapeCache> recentCaches = scrapeCacheRepository
            .findByLastScrapedAtAfterOrderByLastScrapedAtDesc(
                LocalDateTime.now().minusMinutes(ttlSeconds), Limit.of(5));

        List<RetailSourceItemDTO> allCached = recentCaches.stream()
            .filter(this::isFresh)
            .flatMap(c -> c.getResults().stream())
            .toList();

        if (allCached.isEmpty()) {
            return emptyPage(pageNum);
        }
        return paginate(new ArrayList<>(allCached), pageNum);
    }

    private List<RetailSourceItemDTO> rescrapeAndCache(String s, ExecutorService executor) {
        List<RetailSourceItemDTO> overall = findWebListings(s, executor); 

        ScrapeCache existing = scrapeCacheRepository.findBySearchTerm(s).orElse(null);

        ScrapeCache toSave =ScrapeCache.builder()
        .id(existing != null ? existing.getId() : null)
        .searchTerm(s)
        .results(overall)
        .lastScrapedAt(LocalDateTime.now())
        .build();

        ScrapeCache saved =  scrapeCacheRepository.save(toSave);
        cache.put(s, saved); // keep in-memory layer in sync
        return overall;
    }

    public Page<RetailSourceItemDTO> getRetailListingsPage(String searchTerm, Integer pageNum) {
        List<RetailSourceItemDTO> results = findWebListingsCached(searchTerm);
        return paginate(results, pageNum);
    }

    // call this from wherever a game gets added to a user's library so the personalized
    // tab isn't stuck doing a synchronous scrape the first time it's requested.
    public void prewarmListings(String gameTitle) {
        if (!StringUtils.hasText(gameTitle)) return;
        scheduledScraperExecutor.submit(() ->
            findWebListingsCached(gameTitle + " Boardgame", scheduledScraperExecutor));
    }
    
    //RECCOMMENDATION ALGORITHM
    //CURRENT APPROACH: fetch as many games as possible based on 

    @Scheduled(fixedDelayString = "${scrape.cache.refresh.interval.ms:3600000}", initialDelayString = "${scrape.cache.refresh.interval.ms:3600000}")
    public void ScheduledRecommendedScraper(){
        runRecommendedScraper();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        recommendedScraperRunnerExecutor.submit(this::runRecommendedScraper);
    }

    private void runRecommendedScraper(){
        //SHOULDDO: compute most popular first 
        //get first n games 
        List<GameOwnershipCount> currGames = new ArrayList<>(userRepository.findMostOwnedGameIds(NUMOFGAMES)); 
        //fall back 
        List<Boardgame> games = new ArrayList<>();

        if(currGames.size() <   5 ){//5 to keep somewhat of a variety for games
           games = getGloballyPopularGames();
        } else{
            for(GameOwnershipCount x : currGames){
                // get Game from boardgame 

                Optional<Boardgame> bg = boardGameRepository.findById(x.getId());

                if(bg.isEmpty()){
                    logger.warning("Skipping missing boardgame id during scheduled scrape: " + x.getId());
                    continue;
                }
                
                //bg is not empty/ is PRESENT
                games.add(bg.get());
            }
        }

        if (games.isEmpty()) {
            logger.warning("No boardgames available to scrape (empty owned + fallback lists)");
            return;
        }

        //scrape based on current 
        for(Boardgame bg : games){
            logger.info(() -> "Scraping for: " + bg.getTitle() + " boardgames");
            long s = System.currentTimeMillis();
            this.findWebListingsCached(bg.getTitle()+" Boardgame", scheduledScraperExecutor);
            long e = System.currentTimeMillis();

            long tot = e-s;

            logger.info(() -> "completed after: ");
            logger.info(() -> tot+ " ms");
            logger.info(() -> tot/1000+ " s");
        }
    }
    
    @PreDestroy
    public void shutdown() {
        scraperExecutor.shutdown();
        scheduledScraperExecutor.shutdown();
        recommendedScraperRunnerExecutor.shutdown();
    }
}