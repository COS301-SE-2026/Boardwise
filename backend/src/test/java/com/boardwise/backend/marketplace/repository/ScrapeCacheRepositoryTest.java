package com.boardwise.backend.marketplace.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import com.boardwise.backend.SharedMongoContainer;
import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.model.ScrapeCache;

@DisplayName("Scrape Cache Repository Tests")
@DataMongoTest()
public class ScrapeCacheRepositoryTest extends SharedMongoContainer {

    @Autowired
    private ScrapeCacheRepository scrapeCacheRepository;

    private RetailSourceItemDTO buildItem(String retailerName, String toSearch, Double price, float jwValue) {
        String url = "http://validurl.com";
        String imageUrl = "http://validimageurl.com";
        return new RetailSourceItemDTO(toSearch, retailerName, url, price, imageUrl, jwValue);
    }

    private ScrapeCache buildCache(String searchTerm, List<RetailSourceItemDTO> results, LocalDateTime lastScrapedAt) {
        return ScrapeCache.builder()
            .searchTerm(searchTerm)
            .results(results)
            .lastScrapedAt(lastScrapedAt)
            .build();
    }

    @DisplayName("Reset Repository")
    @BeforeEach
    void resetToSomeBaseTest() {
        scrapeCacheRepository.deleteAll();

        ScrapeCache catanCache = buildCache("Catan",
            List.of(buildItem("Takealot", "Catan deluxe", 450.0, 0.6f)),
            LocalDateTime.now());

        ScrapeCache riskCache = buildCache("Risk",
            List.of(),
            LocalDateTime.now().minusMinutes(120));

        scrapeCacheRepository.insert(catanCache);
        scrapeCacheRepository.insert(riskCache);

        assertTrue(scrapeCacheRepository.count() > 0);
    }

    @Test
    @DisplayName("Repository finds a cache entry by search term")
    void shouldFindBySearchTerm() {
        // ARRANGE
        String term = "Catan";

        // ACT
        Optional<ScrapeCache> res = scrapeCacheRepository.findBySearchTerm(term);

        // ASSERT
        assertNotNull(res);
        assertTrue(res.isPresent());
        assertEquals(term, res.get().getSearchTerm());
        assertEquals(1, res.get().getResults().size());
    }

    @Test
    @DisplayName("Repository returns empty when search term has no cache entry")
    void shouldReturnEmptyForUnknownSearchTerm() {
        // ARRANGE
        String term = "SomeGameNobodyScrapedYet";

        // ACT
        Optional<ScrapeCache> res = scrapeCacheRepository.findBySearchTerm(term);

        // ASSERT
        assertNotNull(res);
        assertFalse(res.isPresent());
    }

    @Test
    @DisplayName("Repository keeps lastScrapedAt for staleness checks")
    void shouldPreserveLastScrapedAt() {
        // ARRANGE
        String term = "Risk";

        // ACT
        Optional<ScrapeCache> res = scrapeCacheRepository.findBySearchTerm(term);

        // ASSERT
        assertTrue(res.isPresent());
        assertNotNull(res.get().getLastScrapedAt());
        assertTrue(res.get().getLastScrapedAt().isBefore(LocalDateTime.now().minusMinutes(60)));
    }

    @Test
    @DisplayName("Repository upserts by id instead of creating duplicate rows per search term")
    void shouldUpsertExistingCacheEntryById() {
        // ARRANGE
        ScrapeCache existing = scrapeCacheRepository.findBySearchTerm("Catan").orElseThrow();

        ScrapeCache updated = ScrapeCache.builder()
            .id(existing.getId())
            .searchTerm("Catan")
            .results(List.of(
                buildItem("BobShop", "Catan updated", 399.0, 0.7f),
                buildItem("ToysRus", "Catan updated 2", 410.0, 0.65f)
            ))
            .lastScrapedAt(LocalDateTime.now())
            .build();

        // ACT
        scrapeCacheRepository.save(updated);
        List<ScrapeCache> allCatanEntries = scrapeCacheRepository.findAll().stream()
            .filter(c -> c.getSearchTerm().equals("Catan"))
            .toList();

        // ASSERT
        assertEquals(1, allCatanEntries.size());
        assertEquals(2, allCatanEntries.get(0).getResults().size());
    }
}
