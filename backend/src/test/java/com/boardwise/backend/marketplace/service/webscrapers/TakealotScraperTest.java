package com.boardwise.backend.marketplace.service.webscrapers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;

class TakealotScraperTest {

    private TakealotScraper scraper;

    @BeforeEach
    public void setUp() {
        scraper = new TakealotScraper(new BrowserManager());
    }

    // noResultsFound

    @Test
    public void noResultsFoundPageContainsNoResultsMessageReturnsTrue() {
        String content = "<html>We couldn't find results for \"asdf\"</html>";

        boolean result = scraper.noResultsFound(content);

        assertTrue(result);
    }

    @Test
    public void noResultsFoundPageHasResultsReturnsFalse() {
        String content = "<html><article data-ref='product-card'>Monopoly</article></html>";

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
        assertTrue(scraper.isSponsored("https://www.takealot.com/product/monopoly?offer_pref=123"));
    }

    @Test
    public void isSponsoredUrlDoesNotContainOfferPrefReturnsFalse() {
        assertFalse(scraper.isSponsored("https://www.takealot.com/product/monopoly"));
    }

    @Test
    public void isSponsoredNullUrlReturnsFalse() {
        assertFalse(scraper.isSponsored(null));
    }

    // parsePrice

    @Test
    public void parsePriceValidRandsWithCommaReturnsDouble() {
        Double result = scraper.parsePrice("R1,299.99");

        assertEquals(1299.99, result);
    }

    @Test
    public void parsePriceNoCommaReturnsDouble() {
        Double result = scraper.parsePrice("R450");

        assertEquals(450.0, result);
    }

    @Test
    public void parsePriceNullReturnsNull() {
        Double result = scraper.parsePrice(null);

        assertNull(result);
    }

    @Test
    public void parsePriceGarbageReturnsNullNotException() {
        Double result = scraper.parsePrice("not-a-price");

        assertNull(result);
    }

    // buildOfficialUrl

    @Test
    public void buildOfficialUrlPrependsSite() {
        String result = scraper.buildOfficialUrl("/product/monopoly");

        assertEquals("https://www.takealot.com/product/monopoly", result);
    }

    @Test
    public void buildOfficialUrlNullReturnsNull() {
        String result = scraper.buildOfficialUrl(null);

        assertNull(result);
    }

    // buildItem

    @Test
    public void buildItemValidCardReturnsPopulatedDTO() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem("Monopoly", "Monopoly Classic Board Game", "/product/monopoly","R499.99", null, "https://img.takealot.com/monopoly.jpg");

        assertTrue(result.isPresent());
    }

    @Test
    public void buildItemSponsoredCardReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem("Monopoly", "Monopoly Classic Board Game", "/product/monopoly?offer_pref=1", "R499.99", null, "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemBlankTitleReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem("Monopoly", "   ", "/product/monopoly","R499.99", null, "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemNullTitleReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem("Monopoly", null, "/product/monopoly","R499.99", null, "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemBelowSimilarityThresholdReturnsEmpty() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem("Monopoly", "Completely Unrelated Kitchen Blender","/product/blender", "R499.99", null, "img.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildItemUsesListPriceWhenPresent() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem("Monopoly", "Monopoly Classic Board Game", "/product/monopoly","R499.99", "R599.99", "img.jpg");

        assertTrue(result.isPresent());
        assertEquals(599.99, result.get().price());
    }

    @Test
    public void buildItemFallsBackToActualPriceWhenNoListPrice() {
        Optional<RetailSourceItemDTO> result = scraper.buildItem("Monopoly", "Monopoly Classic Board Game", "/product/monopoly","R499.99", null, "img.jpg");

        assertTrue(result.isPresent());
        assertEquals(499.99, result.get().price());
    }

    // sortBySimilarity

    @Test
    public void sortBySimilaritySortsAscendingByScore() {
        RetailSourceItemDTO low = new RetailSourceItemDTO("A", "Takealot", "url1", 10.0, "img1", 0.5f);
        RetailSourceItemDTO high = new RetailSourceItemDTO("B", "Takealot", "url2", 20.0, "img2", 0.9f);

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