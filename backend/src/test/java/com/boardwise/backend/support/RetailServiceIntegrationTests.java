package com.boardwise.backend.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.model.ScrapeCache;
import com.boardwise.backend.marketplace.repository.ScrapeCacheRepository;
import com.boardwise.backend.marketplace.service.RetailService;
import com.boardwise.backend.marketplace.service.webscrapers.BobShopScraper;
import com.boardwise.backend.marketplace.service.webscrapers.TakealotScraper;
import com.boardwise.backend.marketplace.service.webscrapers.ToysRUsScraper;

@DisplayName("Retail Service Integration Tests")
public class RetailServiceIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private RetailService retailService;

    @Autowired
    private ScrapeCacheRepository scrapeCacheRepository;

    @MockitoBean
    private TakealotScraper ts;

    @MockitoBean
    private BobShopScraper bss;

    @MockitoBean
    private ToysRUsScraper trus;

    @BeforeEach
    void setUp() {
        scrapeCacheRepository.deleteAll();
    }

    RetailSourceItemDTO genValidRetailSourceItemDTO(String retailerName, String toSearch, Double price, float JWValue) {
        String url = "http://validurl.com";
        String imageUrl = "http://validimageurl.com";
        return new RetailSourceItemDTO(toSearch, retailerName, url, price, imageUrl, JWValue);
    }

    @DisplayName("Should scrape, cache, then serve subsequent request from cache without rescraping")
    @Test
    public void shouldCacheAcrossRealSpringContext() {
        // ARRANGE
        String toSearch = "Catan";
        List<RetailSourceItemDTO> tsResults = List.of(genValidRetailSourceItemDTO("Takealot", "Catan 1", 400.0, 0.5f));
        when(ts.scrape(toSearch)).thenReturn(tsResults);
        when(bss.scrape(toSearch)).thenReturn(new ArrayList<>());
        when(trus.scrape(toSearch)).thenReturn(new ArrayList<>());

        // ACT -cache empty, should scrape and persist
        Page<RetailSourceItemDTO> firstPage = retailService.getRetailListingsPage(toSearch, 0);

        // ASSERT - result correct and a real Mongo doc was written 
        assertThat(firstPage.getContent()).hasSize(1);
        ScrapeCache saved = scrapeCacheRepository.findBySearchTerm(toSearch).orElseThrow();
        assertThat(saved.getResults()).hasSize(1);
        assertThat(saved.getLastScrapedAt()).isNotNull();

        // ACT - cache should now be fresh, scrapers shouldn't be hit again
        Page<RetailSourceItemDTO> secondPage = retailService.getRetailListingsPage(toSearch, 0);

        // ASSERT
        assertThat(secondPage.getContent()).hasSize(1);
        org.mockito.Mockito.verify(ts, org.mockito.Mockito.times(1)).scrape(toSearch); // still only called once total
    }

    @DisplayName("Should rescrape when a real stale cache document exists in Mongo")
    @Test
    public void shouldRescrapeStaleCacheFromRealMongo() {
        // ARRANGE - write a stale entry to the real test Mongo instance
        ScrapeCache stale = ScrapeCache.builder()
            .searchTerm("Risk")
            .results(new ArrayList<>())
            .lastScrapedAt(LocalDateTime.now().minusMinutes(120))
            .build();
        scrapeCacheRepository.save(stale);

        List<RetailSourceItemDTO> freshResults = List.of(genValidRetailSourceItemDTO("BobShop", "Risk fresh", 350.0, 0.4f));
        when(ts.scrape("Risk")).thenReturn(new ArrayList<>());
        when(bss.scrape("Risk")).thenReturn(freshResults);
        when(trus.scrape("Risk")).thenReturn(new ArrayList<>());

        // ACT
        Page<RetailSourceItemDTO> page = retailService.getRetailListingsPage("Risk", 0);

        // ASSERT
        assertThat(page.getContent()).hasSize(1);
        ScrapeCache updated = scrapeCacheRepository.findBySearchTerm("Risk").orElseThrow();
        assertThat(updated.getLastScrapedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }
}