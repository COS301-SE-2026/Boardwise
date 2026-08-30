package com.boardwise.backend.marketplace.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

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

    private final ConcurrentHashMap<String, ScrapeCache> cache = new ConcurrentHashMap<>();
    //AUTH
    private final JWTService jwtService;


    private final ScrapeCacheRepository scrapeCacheRepository;
    
    @Value("${scrape.cache.ttl.minutes:60}")
    private long ttlMin;

    public RetailService(ScrapeCacheRepository scrapeCacheRepository,TakealotScraper ts, BobShopScraper bss, ToysRUsScraper trus, UserRepository userRepository, BoardGameRepository boardGameRepository, JWTService jwtService) {
        this.ts = ts;
        this.bss = bss;
        this.trus = trus;
        this.scrapeCacheRepository = scrapeCacheRepository;
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
        this.jwtService = jwtService;
    }

    protected List<RetailSourceItemDTO> findWebListings(String s) {
        if (!StringUtils.hasText(s)) {
            return new ArrayList<>();
        }

        // individual processes happening concurrently
        CompletableFuture<List<RetailSourceItemDTO>> takealotFuture = CompletableFuture.supplyAsync(() -> safeScrape(ts::scrape, s));
        CompletableFuture<List<RetailSourceItemDTO>> bobShopFuture = CompletableFuture.supplyAsync(() -> safeScrape(bss::scrape, s));
        CompletableFuture<List<RetailSourceItemDTO>> toysRUsFuture = CompletableFuture.supplyAsync(() -> safeScrape(trus::scrape, s));

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

        ScrapeCache cached = cache.get(s);
        if (cached != null && isFresh(cached)) {
            return cached.getResults();
        }

        return scrapeCacheRepository.findBySearchTerm(s)
            .filter(this::isFresh)
            .map(c -> {
                cache.put(s, c); // in-memory layer
                return c.getResults();
            })
            .orElseGet(() -> rescrapeAndCache(s));
    }

    private boolean isFresh(ScrapeCache cache) {
        if (cache.getLastScrapedAt() == null) return false;
        long ageMinutes = java.time.Duration.between(cache.getLastScrapedAt(), java.time.LocalDateTime.now()).toMinutes();
        return ageMinutes < ttlMin;
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
    private List<Boardgame> getGloballyPopularGames() {
        List<GameOwnershipCount> topOwned = userRepository.findMostOwnedGameIds(NUMOFGAMES);
 
        List<Boardgame> games = new ArrayList<>();
        if (!topOwned.isEmpty()) {
            List<String> ownedIds = topOwned.stream().map(GameOwnershipCount::getId).toList();
            games.addAll(boardGameRepository.findAllById(ownedIds));
        }
 
        int shortfall = NUMOFGAMES - games.size();
        if (shortfall > 0) {
            List<String> alreadySelectedIds = games.stream().map(Boardgame::getId).toList();
            List<Boardgame> fallbackCandidates = boardGameRepository.findAllBy(Limit.of(shortfall + NUMOFGAMES));
 
            fallbackCandidates.stream()
                .filter(bg -> !alreadySelectedIds.contains(bg.getId()))
                .limit(shortfall)
                .forEach(games::add);
        }
 
        return games;
    }

    private Page<RetailSourceItemDTO> emptyPage(Integer pageNum) {
        int page = (pageNum == null || pageNum < 0) ? 0 : pageNum;
        return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, PAGESIZE), 0);
    }
    
    public Page<RetailSourceItemDTO> getPersonalizedRetailListings(String token, Integer pageNum) {
        //Extract userId
        ObjectId userId = jwtService.extractUserId(token);
        //Get the users details 
        Optional<User> user = userRepository.findById(userId.toString());

        if (user.isEmpty()){
            logger.info(() -> "Error while trying to find user for personalised listings: " + userId.toString());
            return emptyPage(pageNum);
        }

        //Look at what games the user has 
        List<String> ownedGameIds = user.get().getOwnedGames();
        List<Boardgame> games; // games with the names

        if(ownedGameIds != null && !ownedGameIds.isEmpty()){
            List<String> topNGames = ownedGameIds.stream().limit(NUMOFGAMES).toList();
            games = boardGameRepository.findAllById(topNGames);
        }
        else{
            //no inventory probably a new user
            games = getGloballyPopularGames();
        }

        if(games.isEmpty()){
            return emptyPage(pageNum);
        }

        List<RetailSourceItemDTO> combined = new ArrayList<>();
        for (Boardgame bg : games) {
            combined.addAll(findWebListingsCached(bg.getTitle() + " Boardgame"));
        }

        return paginate(combined, pageNum);
    }

    private List<RetailSourceItemDTO> rescrapeAndCache(String s) {
        List<RetailSourceItemDTO> overall = findWebListings(s); 

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
    
    //RECCOMMENDATION ALGORITHM
    //CURRENT APPROACH: fetch as many listings as possible based on 
    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;
     
    //number of Games to search for 
    private final int NUMOFGAMES = 25;

    @Scheduled(fixedDelayString = "${scrape.cache.refresh.interval.ms:3600000}" ,initialDelayString = "${scrape.cache.refresh.interval.ms:3600000}")
    public void ScheduledRecommendedScraper(){
        //SHOULDDO: compute most popular first 
        //get first n games 
        List<GameOwnershipCount> currGames = new ArrayList<>(userRepository.findMostOwnedGameIds(NUMOFGAMES)); // mock
        //fall back 
        List<Boardgame> games = new ArrayList<>();

        if(currGames.size() < 3){//3 to keep somewhat of a variety for games
           games = getGloballyPopularGames();
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