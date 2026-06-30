package com.boardwise.backend.scraper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import org.springframework.stereotype.Service;

import com.boardwise.backend.scraper.dtos.ScrapeResponse;
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

    public List<ScrapeResponse> scrape(String toSearch) {
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
            List<ScrapeResponse> matching = new ArrayList<>();

            if(cards.isEmpty()){
                return matching;// for some reason?? should lowkey an exception because wow
            }

            for(Locator card : cards){
                Locator titleElement = card.locator("a.product-item-link").first();          
                if (titleElement.count() == 0) continue;
                
                
                String title = titleElement.innerText().trim();

                String url = card.locator("a.product-item-link").first().getAttribute("href");

            // Jaro-Winkler - similarity between 2 sequences
                float val = JaroWinklerSimilarity(toSearch,title);
                if(val >= stringMatch && !url.contains("offer_pref")){// remove sponsored items
                    SimpleEntry<String, Float> toAdd = new SimpleEntry<String,Float>(url, val);
                    matching.add(new ScrapeResponse(site,toAdd));
                }
            
                if(matching.size() >= MAXNUMITEMS) break;
            }
            
            page.close();

            matching.sort(Comparator.comparingDouble(r-> r.details().getValue())); // sort in terms of float
            return matching;

        } catch (Exception e) {
            e.printStackTrace();
        }
        //if you ever exit this... wow
        throw new RuntimeException("somehow reached a place you shouldn't have ");
    }
}