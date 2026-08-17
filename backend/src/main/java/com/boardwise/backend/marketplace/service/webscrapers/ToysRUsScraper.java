package com.boardwise.backend.marketplace.service.webscrapers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class ToysRUsScraper implements WebScraper {

    public ToysRUsScraper(){} 

    private final int MAXNUMITEMS = 15;
    private String searchSelector = "input[placeholder='The search for fun starts here...']";
    private final String site = "https://www.toysrus.co.za/";
    private final String RETAILERNAME="ToysRUs";

    @Override
    public List<RetailSourceItemDTO> scrape(String toSearch) {
        if(toSearch== null || toSearch.isBlank()){
            return null;
        }
        try(Playwright playwright = Playwright.create()){
            //chromium
            Browser chromium =  playwright.chromium().launch();
            BrowserContext context = chromium.newContext();
            Page page = context.newPage();

            //website
            page.navigate(site);
            page.waitForSelector(searchSelector);


            //find search bar
            Locator searchBar = page.getByPlaceholder("The search for fun starts here...");

            if(searchBar.count() == 0){
                throw new RuntimeException("Error while trying to find the search bar for Toys R US");
            }


            //update value found in search bar
            searchBar.fill(toSearch);
            searchBar.press("Enter");

            // page.waitForSelector("article[data-ref='product-card']");

            String contentOfPage = page.content();

            if(contentOfPage.contains("We couldn’t find anything to match your search.")){ // 
                return null;
            }

            //find article 
            List<Locator> cards =  page.locator("li.product-item").all();

            if(cards.isEmpty()){
                return null;// for some reason?? should lowkey an exception because wow
            }

            List<RetailSourceItemDTO> retailSourceItemDTOs = new ArrayList<>();

    
            for(Locator card : cards){
                Locator titleElement = card.locator("a.product-item-link").first();          
                if (titleElement.count() == 0) continue;
                
                
                String title = titleElement.innerText().trim();

                String url = card.locator("a.product-item-link").first().getAttribute("href");
            
                Locator priceLoc = card.locator(".price-wrapper").first();
                String priceRaw = priceLoc.count() > 0 ? priceLoc.getAttribute("data-price-amount") : null;

                String imageUrl = card.getAttribute("data-image");

                if (imageUrl == null || imageUrl.isBlank() || imageUrl.contains("placeholder")) {
                    imageUrl = null; 
                }


                buildItem(toSearch, title, url, priceRaw, imageUrl).ifPresent(retailSourceItemDTOs::add);

                if(retailSourceItemDTOs.size() >= MAXNUMITEMS) break;
            }
            
            page.close();

            sortBySimilarity(retailSourceItemDTOs); // sort in terms of float
            return retailSourceItemDTOs;

        } catch (Exception e) {
            e.printStackTrace();
        }
        //if you ever exit this... wow
        throw new RuntimeException("somehow reached a place you shouldn't have ");
    }

protected boolean noResultsFound(String pageContent) {
        return pageContent != null && pageContent.contains("We couldn\u2019t find anything to match your search.");
    }

    protected boolean isSponsored(String url) {
        return url != null && url.contains("offer_pref");
    }

    protected String cleanImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.contains("placeholder")) {
            return null;
        }
        return imageUrl;
    }

    protected Double parsePrice(String priceRaw) {
        if (priceRaw == null) {
            return null;
        }
        try {
            return Double.parseDouble(priceRaw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Optional<RetailSourceItemDTO> buildItem(
            String toSearch, String rawTitle, String url, String priceRaw, String rawImageUrl) {

        if (isSponsored(url)) {
            return Optional.empty();
        }

        String title = rawTitle == null ? "" : rawTitle.trim();
        if (title.isBlank()) {
            return Optional.empty();
        }

        Double price = parsePrice(priceRaw);
        String imageUrl = cleanImageUrl(rawImageUrl);

        // Jaro-Winkler - similarity between 2 sequences
        float val = JaroWinklerSimilarity(toSearch, title);

        if (val < STRINGMATCH) {
            return Optional.empty();
        }

        return Optional.of(new RetailSourceItemDTO(title, RETAILERNAME, url, price, imageUrl, val));
    }

    protected void sortBySimilarity(List<RetailSourceItemDTO> items) {
        items.sort(Comparator.comparingDouble(RetailSourceItemDTO::JaroWinklerSimilarityScore));
    }

}