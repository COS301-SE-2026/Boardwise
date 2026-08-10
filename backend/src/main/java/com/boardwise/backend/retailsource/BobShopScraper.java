package com.boardwise.backend.retailsource;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.boardwise.backend.retailsource.dtos.ScrapeResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class BobShopScraper implements WebScraper{
   
    public BobShopScraper(){} 
    
    private final int MAXNUMITEMS = 15;
    private final String site = "https://www.bobshop.co.za";

    
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

            //find search bar
            Locator searchBar = page.getByPlaceholder("Search for anything");

            if(searchBar.count() == 0){
                throw new RuntimeException("Error while trying to find the search bar on Bob Shop");
            }

            //update value found in search bar
            searchBar.fill(toSearch);
            searchBar.press("Enter");
            page.waitForLoadState();
            page.waitForSelector("a.product-card-container");

            //find article 
            List<Locator> cards = page.locator("a.product-card-container").all();
            System.out.println(cards.size());

            List<ScrapeResponse> matching = new ArrayList<>();

            if(cards.isEmpty()){
                return matching;// for some reason?? should lowkey an exception because wow
            }

            for(Locator card : cards){
                String classAttr = card.getAttribute("class");
                if (classAttr != null && classAttr.contains("sponsored")) continue;

                String title = card.locator("div.product-card-title").innerText().trim();
                if (title.isBlank()) continue;

                String url = card.getAttribute("href");

                // Jaro-Winkler - similarity between 2 sequences
                float val = JaroWinklerSimilarity(toSearch,title);

                
                if(val >= stringMatch){
                    SimpleEntry<String, Float> toAdd = new SimpleEntry<String,Float>(url, val);
                    matching.add(new ScrapeResponse(site,toAdd));
                }
            
                if(matching.size() >= MAXNUMITEMS) break;
            }
            
            page.close();

            matching.sort(Comparator.comparingDouble(r-> r.details().getValue())); // sort in terms of float

            for(ScrapeResponse x: matching){
                System.out.println(x.details().getKey());
            }
            return matching;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
