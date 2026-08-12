package com.boardwise.backend.retailsource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import org.springframework.stereotype.Service;

import com.boardwise.backend.retailsource.dtos.RetailSourceItemDTO;
import com.boardwise.backend.retailsource.dtos.ScrapeResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class TakealotScraper implements WebScraper {

    public TakealotScraper(){} 
    
    private final int MAXNUMITEMS = 15;
    private String searchSelector = "input[placeholder='Search for products, brands...']";
    private final String site = "https://www.takealot.com";
    private final String RETAILERNAME = "Takealot";

    @Override
    public List<RetailSourceItemDTO> scrape(String toSearch) {
        if(toSearch.isBlank()){
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
            Locator searchBar = page.getByPlaceholder("Search for products, brands...");

            if(searchBar.count() == 0){
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

                String price = card.locator("[data-ref='price'] .currency").innerText();
                double actualPrice = parsePrice(price);                


                Locator listPriceLoc = card.locator("[data-ref='list-price'] .currency");
                Double listPrice = listPriceLoc.count() > 0 ? parsePrice(listPriceLoc.innerText()) : null;
                
                String imageUrl = card.locator("[data-ref='product-image']").getAttribute("src");

                String title = card.locator("[data-ref='panel-content'] h4").innerText();
                String url = card.locator("a[title='Go to product details']").getAttribute("href");

                Double storedPrice = (listPrice == null)? actualPrice : Double.valueOf(listPrice);
                // Jaro-Winkler - similarity between 2 sequences
                float val = JaroWinklerSimilarity(toSearch,title);

                if(val >= stringMatch && !url.contains("offer_pref")){// remove sponsored items
                    String officialUrl = site.substring(0,site.length()) + url;
                    retailSourceItemDTOs.add(new RetailSourceItemDTO(title,RETAILERNAME, officialUrl, storedPrice, imageUrl, val));
                }
            
                if(retailSourceItemDTOs.size() >= MAXNUMITEMS) break;
            }
            page.close();

            retailSourceItemDTOs.sort(Comparator.comparingDouble(r -> r.JaroWinklerSimilarityScore())); // sort in terms of float
            System.out.println(retailSourceItemDTOs);
            return retailSourceItemDTOs;

        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("somehow reached a place you shouldn't have ");
    }

    private Double parsePrice(String raw) {
        return Double.valueOf(raw.replace("R", "").replace(",", "").trim());
    }
}