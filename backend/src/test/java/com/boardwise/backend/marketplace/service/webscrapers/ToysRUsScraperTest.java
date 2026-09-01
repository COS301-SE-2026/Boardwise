package com.boardwise.backend.marketplace.service.webscrapers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;

class ToysRUsScraperTest {

    private ToysRUsScraper scraper;

    @BeforeEach
    public void setUp() {
        scraper = new ToysRUsScraper(new BrowserManager());
    }

    // noResultsFound

    @Test
    public void noResultsFoundPageContainsNoResultsMessageReturnsTrue() {
        // ARRANGE
        String content = "<html>We couldn\u2019t find anything to match your search.</html>";

        // ACT
        boolean result = scraper.noResultsFound(content);

        // ASSERT
        assertTrue(result);
    }

    @Test
    public void noResultsFoundPageHasResultsReturnsFalse() {
        String content = "<html><li class='product-item'>Monopoly</li></html>";

        boolean result = scraper.noResultsFound(content);

        assertFalse(result);
    }

    @Test
    public void noResultsFoundNullContentReturnsFalse() {
        boolean result = scraper.noResultsFound(null);

        assertFalse(result);
    }

    // isSponsored

    @Test
    public void isSponsoredUrlContainsOfferPrefReturnsTrue() {
        assertTrue(scraper.isSponsored("https://toysrus.co.za/product?offer_pref=123"));
    }

    @Test
    public void isSponsoredUrlDoesNotContainOfferPrefReturnsFalse() {
        assertFalse(scraper.isSponsored("https://toysrus.co.za/product/monopoly"));
    }

    @Test
    public void isSponsoredNullUrlReturnsFalse() {
        assertFalse(scraper.isSponsored(null));
    }

    // cleanImageUrl

    @Test
    public void cleanImageUrlValidUrlReturnsUrl() {
        String result = scraper.cleanImageUrl("https://img.toysrus.co.za/monopoly.jpg");

        assertEquals("https://img.toysrus.co.za/monopoly.jpg", result);
    }

    @Test
    public void cleanImageUrlPlaceholderUrlReturnsNull() {
        String result = scraper.cleanImageUrl("https://img.toysrus.co.za/placeholder.jpg");

        assertNull(result);
    }

    @Test
    public void cleanImageUrlBlankUrlReturnsNull() {
        String result = scraper.cleanImageUrl("   ");

        assertNull(result);
    }

    @Test
    public void cleanImageUrlNullUrlReturnsNull() {
        String result = scraper.cleanImageUrl(null);

        assertNull(result);
    }

    // parsePrice

    @Test
    public void parsePriceValidPriceReturnsDouble() {
        Double result = scraper.parsePrice("699.99");

        assertEquals(699.99, result);
    }

    @Test
    public void parsePriceNullPriceReturnsNull() {
        Double result = scraper.parsePrice(null);

        assertNull(result);
    }

    @Test
    public void parsePriceGarbagePriceReturnsNullNotException() {
        Double result = scraper.parsePrice("not-a-price");

        assertNull(result);
    }

    // buildItem

    @Test
    public void buildItemValidCardReturnsPopulatedDTO() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Monopoly", "Monopoly Classic Board Game",
                "https://toysrus.co.za/monopoly", "499.99",
                "https://img.toysrus.co.za/monopoly.jpg");

        assertTrue(result.isPresent());
    }

    @Test
    public void buildItemSponsoredCardReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Monopoly", "Monopoly Classic Board Game",
                "https://toysrus.co.za/monopoly?offer_pref=1", "499.99",
                "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemBlankTitleReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Monopoly", "   ", "https://toysrus.co.za/monopoly",
                "499.99", "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemNullTitleReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Monopoly", null, "https://toysrus.co.za/monopoly",
                "499.99", "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemBelowSimilarityThresholdReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Monopoly", "Completely Unrelated Kitchen Blender",
                "https://toysrus.co.za/blender", "499.99", "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemMissingPriceReturnsDtoWithNullPrice() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Monopoly", "Monopoly Classic Board Game",
                "https://toysrus.co.za/monopoly", null, "img.jpg");

        assertTrue(result.isPresent());
    }

    @Test
    public void buildItemPlaceholderImageReturnsDtoWithNullImage() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem(
                "Monopoly", "Monopoly Classic Board Game",
                "https://toysrus.co.za/monopoly", "499.99",
                "https://img.toysrus.co.za/placeholder.jpg");

        assertTrue(result.isPresent());
    }

    // sortBySimilarity

    @Test
    public void sortBySimilaritySortsAscendingByScore() {
        RetailSourceItemDTO low = new RetailSourceItemDTO("A", "ToysRUs", "url1", 10.0, "img1", 0.5f);
        RetailSourceItemDTO high = new RetailSourceItemDTO("B", "ToysRUs", "url2", 20.0, "img2", 0.9f);

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