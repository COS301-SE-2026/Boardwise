package com.boardwise.backend.marketplace.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.models.ScrapeCache;
import com.boardwise.backend.marketplace.repository.ScrapeCacheRepository;
import com.boardwise.backend.marketplace.service.webscrapers.BobShopScraper;
import com.boardwise.backend.marketplace.service.webscrapers.TakealotScraper;
import com.boardwise.backend.marketplace.service.webscrapers.ToysRUsScraper;


@DisplayName("Retail Service Tests")
@ExtendWith(MockitoExtension.class) // auto create/inject mocks
public class RetailServiceTest {
    
    @InjectMocks
    private RetailService retailService;

    @Mock
    private ScrapeCacheRepository scrapeCacheRepository;

    @Mock 
    TakealotScraper ts;
    
    @Mock
    BobShopScraper bss;

    @Mock
    ToysRUsScraper trus;

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(retailService, "ttlSeconds", 3600L);
    }


    //helper: Generate random RetailSericeItemDTO

    RetailSourceItemDTO genValidRetailSourceItemDTO(String retailerName, String toSearch, Double price, float JWValue ){
        List<String> valid = List.of("Takealot", "BobShop", "ToysRUs");
        if (retailerName == null || retailerName.isBlank() || valid.stream().noneMatch(retailerName::equalsIgnoreCase)) {
            throw new IllegalArgumentException("Invalid Retailer Name");
        }
        String url = "http://validurl.com";
        String imageUrl =  "http://validimageurl.com";
        return new RetailSourceItemDTO(toSearch, retailerName, url, price, imageUrl, JWValue);   
    }

    @DisplayName("Should return a list of retail source item DTOS on success")
    @Test
    public void shouldReturnAListOfRetailSourceItemDTOs(){
        //ARRANGE 
        String toSearch = "ExampleString";

        List <RetailSourceItemDTO> tsResults = new ArrayList<>();
        tsResults.add(genValidRetailSourceItemDTO("Takealot","Monopoly board game Test 1 ", 500.00, 0.31f));
        tsResults.add(genValidRetailSourceItemDTO("Takealot","Monopoly board game Test 2 ", 247.24, 0.32f));

        List <RetailSourceItemDTO> bssResults = new ArrayList<>();
        bssResults.add(genValidRetailSourceItemDTO("BobShop","Monopoly board game Test 3 ", 123.53, 0.57f));
        bssResults.add(genValidRetailSourceItemDTO("BobShop","Monopoly board game Test 4 ", 347.24, 0.42f));

        List <RetailSourceItemDTO> trusResults = new ArrayList<>();
        trusResults.add(genValidRetailSourceItemDTO("ToysRus","Monopoly board game Test 5 ", 543.21, 0.81f));
        trusResults.add(genValidRetailSourceItemDTO("ToysRus","Monopoly board game Test 6 ", 299.24, 0.02f));

        when(ts.scrape(toSearch)).thenReturn(tsResults);
        when(trus.scrape(toSearch)).thenReturn(trusResults);
        when(bss.scrape(toSearch)).thenReturn(bssResults);

        //ACT
        List <RetailSourceItemDTO> finalList = retailService.findWebListings(toSearch);

        //ASSERT 
        assertNotNull(finalList);
        assertFalse(finalList.isEmpty());
        assertEquals(6, finalList.size());
    }

    @DisplayName("Should return an empty list of retail source item DTOS on success")
    @Test
    public void shouldReturnAnEmptyList(){
        //ARRANGE 
        String toSearch = "ExampleString";
        when(ts.scrape(toSearch)).thenReturn(null);
        when(trus.scrape(toSearch)).thenReturn(null);
        when(bss.scrape(toSearch)).thenReturn(null);

        //ACT
        List <RetailSourceItemDTO> finalList = retailService.findWebListings(toSearch);

        //ASSERT 
        assertNotNull(finalList);
        assertTrue(finalList.isEmpty());
    }
    
    @DisplayName("Should return partial results when one scraper throws")
    @Test
    public void shouldReturnPartialResultsWhenOneScraperFails() {
        // ARRANGE
        String toSearch = "ExampleString";
 
        List<RetailSourceItemDTO> tsResults = new ArrayList<>();
        tsResults.add(genValidRetailSourceItemDTO("Takealot", "Monopoly board game Test 1 ", 500.00, 0.31f));
 
        when(ts.scrape(toSearch)).thenReturn(tsResults);
        when(bss.scrape(toSearch)).thenThrow(new RuntimeException("BobShop is down"));
        when(trus.scrape(toSearch)).thenReturn(new ArrayList<>());
 
        // ACT
        List<RetailSourceItemDTO> finalList = retailService.findWebListings(toSearch);
 
        // ASSERT
        assertNotNull(finalList);
        assertEquals(1, finalList.size());
    }
 
    @DisplayName("Should return empty list without calling scrapers when query is blank")
    @Test
    public void shouldShortCircuitOnBlankQuery() {
        // ACT
        List<RetailSourceItemDTO> finalList = retailService.findWebListings("   ");
 
        // ASSERT
        assertNotNull(finalList);
        assertTrue(finalList.isEmpty());
        verifyNoInteractions(ts, bss, trus);
    }

    @DisplayName("Should serve from cache and skip scrapers when cache is fresh")
    @Test
    public void shouldServeFromCacheWhenFresh() {
        // ARRANGE
        String toSearch = "Catan";
        List<RetailSourceItemDTO> cachedResults = new ArrayList<>();
        cachedResults.add(genValidRetailSourceItemDTO("Takealot", "Catan cached", 400.0, 0.5f));

        ScrapeCache freshCache = ScrapeCache.builder()
            .id("abc123")
            .searchTerm(toSearch)
            .results(cachedResults)
            .lastScrapedAt(LocalDateTime.now().minusMinutes(5)) // well within 60min TTL
            .build();

        when(scrapeCacheRepository.findBySearchTerm(toSearch)).thenReturn(Optional.of(freshCache));

        // ACT
        List<RetailSourceItemDTO> result = retailService.findWebListingsCached(toSearch);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        verifyNoInteractions(ts, bss, trus); // scrapers never called on cache hit
        verify(scrapeCacheRepository, never()).save(org.mockito.ArgumentMatchers.any()); // no write on read-only hit
    }

    @DisplayName("Should rescrape and save when cache is stale")
    @Test
    public void shouldRescrapeWhenCacheIsStale() {
        // ARRANGE
        String toSearch = "Risk";
        ScrapeCache staleCache = ScrapeCache.builder()
            .id("xyz789")
            .searchTerm(toSearch)
            .results(new ArrayList<>())
            .lastScrapedAt(LocalDateTime.now().minusMinutes(120)) // older than 60min TTL
            .build();

        List<RetailSourceItemDTO> freshResults = new ArrayList<>();
        freshResults.add(genValidRetailSourceItemDTO("BobShop", "Risk fresh", 350.0, 0.4f));

        when(scrapeCacheRepository.findBySearchTerm(toSearch)).thenReturn(Optional.of(staleCache));
        when(ts.scrape(toSearch)).thenReturn(new ArrayList<>());
        when(bss.scrape(toSearch)).thenReturn(freshResults);
        when(trus.scrape(toSearch)).thenReturn(new ArrayList<>());
        when(scrapeCacheRepository.save(any(ScrapeCache.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        List<RetailSourceItemDTO> result = retailService.findWebListingsCached(toSearch);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ts, times(1)).scrape(toSearch);
        verify(bss, times(1)).scrape(toSearch);
        verify(trus, times(1)).scrape(toSearch);

        ArgumentCaptor<ScrapeCache> captor = ArgumentCaptor.forClass(ScrapeCache.class);
        verify(scrapeCacheRepository, times(1)).save(captor.capture());
        assertEquals("xyz789", captor.getValue().getId()); // reused existing doc's id, no duplicate row
        assertEquals(toSearch, captor.getValue().getSearchTerm());
    }
 
    @DisplayName("Should rescrape and save when no cache entry exists yet")
    @Test
    public void shouldRescrapeWhenNoCacheEntryExists() {
        // ARRANGE
        String toSearch = "Uno";
        List<RetailSourceItemDTO> freshResults = new ArrayList<>();
        freshResults.add(genValidRetailSourceItemDTO("ToysRus", "Uno fresh", 89.99, 0.6f));

        when(scrapeCacheRepository.findBySearchTerm(toSearch)).thenReturn(Optional.empty());
        when(ts.scrape(toSearch)).thenReturn(new ArrayList<>());
        when(bss.scrape(toSearch)).thenReturn(new ArrayList<>());
        when(trus.scrape(toSearch)).thenReturn(freshResults);
        when(scrapeCacheRepository.save(any(ScrapeCache.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        List<RetailSourceItemDTO> result = retailService.findWebListingsCached(toSearch);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());

        ArgumentCaptor<ScrapeCache> captor = ArgumentCaptor.forClass(ScrapeCache.class);
        verify(scrapeCacheRepository, times(1)).save(captor.capture());
        assertEquals(null, captor.getValue().getId()); // no existing doc, new insert
    }

}

