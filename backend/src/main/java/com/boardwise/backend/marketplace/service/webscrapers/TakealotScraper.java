package com.boardwise.backend.marketplace.service.webscrapers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Service
public class TakealotScraper implements WebScraper {

    public TakealotScraper(BrowserManager browserManager){
        this.browserManager = browserManager;
    } 
    private final static  Logger logger = Logger.getLogger(TakealotScraper.class.getName());
    private final int MAXNUMITEMS = 45;
    private final String searchSelector = "input[placeholder='Search for products, brands...']";
    private final String site = "https://www.takealot.com";
    private final String RETAILERNAME = "Takealot";
    private final BrowserManager browserManager;

    @Override
    public List<RetailSourceItemDTO> scrape(String toSearch) {
        long start = System.currentTimeMillis();

        System.out.println("TAKEALOT SCRAPER STARTED");

        if(toSearch == null || toSearch.isBlank()){
            logger.info("Error while scraping Takealot blank search bar");
            return null;
        }
        Browser b = browserManager.getBrowser();
        try(BrowserContext context = b.newContext()){
            //chromium
            Page page = context.newPage();
            context.setDefaultTimeout(30000);

            //website
            page.navigate(site);
            page.waitForSelector(searchSelector);

            //find search bar
            Locator searchBar = page.getByPlaceholder("Search for products, brands...");

            if(searchBar.count() == 0){
                logger.info("Error while looking for a search bar on Takealot");
                throw new RuntimeException("Error while trying to find the search bar on Takealot");
            }

            //update value found in search bar
            searchBar.fill(toSearch);
            searchBar.press("Enter");
            page.waitForSelector("article[data-ref='product-card']");

            String contentOfPage = page.content();

            if(contentOfPage.contains("We couldn't find results for")){ // 
                return null;// if its null no results were found
            }

            //find article 
            List<Locator> cards =  page.locator("article[data-ref='product-card']").all();
            List<RetailSourceItemDTO> retailSourceItemDTOs = new ArrayList<>();

            if(cards.isEmpty()){
                return null;// for some reason?? should lowkey an exception because wow
            }

            for(Locator card : cards){

                String priceRaw = card.locator("[data-ref='price'] .currency").innerText();

                Locator listPriceLoc = card.locator("[data-ref='list-price'] .currency");
                String listPriceRaw = listPriceLoc.count() > 0 ? listPriceLoc.innerText() : null;
                
                String imageUrl = card.locator("[data-ref='product-image']").getAttribute("src");

                String title = card.locator("[data-ref='panel-content'] h4").innerText();
                String url = card.locator("a[title='Go to product details']").getAttribute("href");

                // Jaro-Winkler - similarity between 2 sequences
            
                buildItem(toSearch, title, url, priceRaw, listPriceRaw, imageUrl).ifPresent(retailSourceItemDTOs::add);

                if(retailSourceItemDTOs.size() >= MAXNUMITEMS) break;
            }
            page.close();

            sortBySimilarity(retailSourceItemDTOs); // sort in terms of float
            System.out.println("TAKEALOT SCRAPER FINISHED AFTER:");

            System.out.println((System.currentTimeMillis()-start)/1000 + "s");
            return retailSourceItemDTOs;

        } catch (Exception e) {
            logger.info("Error while scraping Takealot");
            throw new RuntimeException(toSearch);
        }
    }

 protected boolean noResultsFound(String pageContent) {
        return pageContent != null && pageContent.contains("We couldn't find results for");
    }

    protected boolean isSponsored(String url) {
        return url != null && url.contains("offer_pref");
    }

    protected Double parsePrice(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Double.valueOf(raw.replace("R", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected String buildOfficialUrl(String relativeUrl) {
        if (relativeUrl == null) {
            return null;
        }
        return site + relativeUrl;
    }

    protected Optional<RetailSourceItemDTO> buildItem(
            String toSearch, String rawTitle, String relativeUrl,
            String priceRaw, String listPriceRaw, String imageUrl) {

        String officialUrl = buildOfficialUrl(relativeUrl);

        if (isSponsored(officialUrl)) {
            return Optional.empty();
        }

        String title = rawTitle == null ? "" : rawTitle.trim();
        if (title.isBlank()) {
            return Optional.empty();
        }

        Double actualPrice = parsePrice(priceRaw);
        Double listPrice = parsePrice(listPriceRaw);
        Double storedPrice = (listPrice == null) ? actualPrice : listPrice;

        // Jaro-Winkler - similarity between 2 sequences
        float val = JaroWinklerSimilarity(toSearch, title);

        if (val < STRINGMATCH) {
            return Optional.empty();
        }

        return Optional.of(new RetailSourceItemDTO(title, RETAILERNAME, officialUrl, storedPrice, imageUrl, val));
    }

    protected void sortBySimilarity(List<RetailSourceItemDTO> items) {
        items.sort(Comparator.comparingDouble(RetailSourceItemDTO::JaroWinklerSimilarityScore));
    }
}