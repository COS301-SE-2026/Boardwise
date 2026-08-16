package com.boardwise.backend.marketplace.service.webscrapers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;


class BobShopScraperTest {

    private BobShopScraper scraper;

    @BeforeEach
    public void setUp() {
        scraper = new BobShopScraper();
    }

    // parsePrice

    @Test
    public void parsePriceValidRandsAndCentsReturnsCorrectDouble() {
        // ARRANGE
        String rands = "R1,000";
        String cents = "99";

        // ACT
        Double result = scraper.parsePrice(rands, cents);

        // ASSERT
        assertEquals(1000.99, result);
    }

    @Test
    public void parsePriceNoCommaReturnsCorrectDouble() {
        Double result = scraper.parsePrice("R450", "00");

        assertEquals(450.00, result);
    }

    @Test
    public void parsePriceNullRandsReturnsNull() {
        Double result = scraper.parsePrice(null, "99");

        assertNull(result);
    }

    @Test
    public void parsePriceNullCentsReturnsNull() {
        Double result = scraper.parsePrice("R450", null);

        assertNull(result);
    }

    @Test
    public void parsePriceGarbageInputReturnsNullNotException() {
        Double result = scraper.parsePrice("R--", "xx");

        assertNull(result);
    }

    // isSponsored

    @Test
    public void isSponsoredClassContainsSponsoredReturnsTrue() {
        assertTrue(scraper.isSponsored("product-card-container sponsored"));
    }

    @Test
    public void isSponsoredClassDoesNotContainSponsoredReturnsFalse() {
        assertFalse(scraper.isSponsored("product-card-container"));
    }

    @Test
    public void isSponsoredNullClassReturnsFalse() {
        assertFalse(scraper.isSponsored(null));
    }

    // buildItem

    @Test
    public void buildItemValidCardReturnsPopulatedDTO() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Catan", "Catan Board Game", "product-card-container",
                "/products/catan", "R699", "00", "https://img.example/catan.jpg");

        assertTrue(result.isPresent());
    }

    @Test
    public void buildItemSponsoredCardReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Catan", "Catan Board Game", "product-card-container sponsored",
                "/products/catan", "R699", "00", "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemBlankTitleReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Catan", "   ", "product-card-container",
                "/products/catan", "R699", "00", "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemNullTitleReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Catan", null, "product-card-container",
                "/products/catan", "R699", "00", "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemBelowSimilarityThresholdReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Catan", "Completely Unrelated Kitchen Blender", "product-card-container",
                "/products/blender", "R699", "00", "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemMissingPriceElementReturnsDtoWithNullPrice() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Catan", "Catan Board Game", "product-card-container",
                "/products/catan", null, null, "img.jpg");

        assertTrue(result.isPresent());    }

    // sortBySimilarity

    @Test
    public void sortBySimilaritySortsAscendingByScore() {
        RetailSourceItemDTO low = new RetailSourceItemDTO("A", "Bobshop", "url1", 10.0, "img1", 0.5f);
        RetailSourceItemDTO high = new RetailSourceItemDTO("B", "Bobshop", "url2", 20.0, "img2", 0.9f);

        List<RetailSourceItemDTO> items = new ArrayList<>(List.of(high, low));

        scraper.sortBySimilarity(items);

        assertEquals(low, items.get(0));
        assertEquals(high, items.get(1));
    }

    @Test
    public void sortBySimilarityEmptyListDoesNotThrow() {
        List<RetailSourceItemDTO> items = new ArrayList<>();

        assertDoesNotThrow(() -> scraper.sortBySimilarity(items));
    }
}