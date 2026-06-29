package com.boardwise.backend.scraper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Service;

import com.boardwise.backend.scraper.dtos.ScrapeResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class TakealotScraper implements WebScraper {
    public TakealotScraper(){}
    
    private double stringMatch = 0.4; // >=40% string match gets Returned
    private final String site = "https://www.takealot.com";

    public List<ScrapeResponse> scrape(String toSearch) {
        System.out.println(toSearch);
        if(toSearch.isBlank()){
            return null;
        }
        try(Playwright playwright = Playwright.create()){
            //chromium
            Browser chromium =  playwright.chromium().launch();
            BrowserContext context = chromium.newContext();
            Page page = context.newPage();

            //website
            page.navigate("https://www.takealot.com/all?_sb=1&_r=1&qsearch=volkano%20headphones&via=suggestions&_si=ab92cbd997f7d434d9c6f023068e673e");

            //find search bar
            Locator searchBar = page.getByPlaceholder("Search for products, brands...");

            //update value found in search bar
            searchBar.fill(toSearch);

        
            page.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        //Levenshtein distance - distance between two strings

        // Jaro-Winkler - similarity betwen 2 sequences
    
        return null;
    }

    public static void main(String[] args){
        WebScraper ws = new TakealotScraper();
        ws.scrape("Iphone 16");
    }
}
