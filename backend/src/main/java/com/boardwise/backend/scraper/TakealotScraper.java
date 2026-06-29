package com.boardwise.backend.scraper;
import com.boardwise.backend.vault.repository.EditEventRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Path testPdf = Path.of("backend/src/main/java/com/boardwise/backend/scraper/stuff.pdf");

    private double stringMatch = 0.4; // >=40% string match gets Returned
    private String searchSelector = "input[placeholder='Search for products, brands...']";
    private Map<Integer,String> map = new HashMap<>(); 
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

            //Levenshtein distance - distance between two strings
            System.out.println(LevenshteinDistance("Bao", "Boo"));
            // Jaro-Winkler - similarity between 2 sequences

            Files.write(testPdf, page.pdf());
            page.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private float LevenshteinDistance(String a, String b){// STRAIGHT FROM WIKIPEDIA

        if(a.length() == 0) return Math.abs(b.length());// interesting
        if(b.length() == 0) return Math.abs(a.length());// interesting

        if(a.charAt(0) == b.charAt(0)){
            return 0 + LevenshteinDistance(a.substring(1),b.substring(1));
        }

        return 1 + Math.min(LevenshteinDistance(a.substring(1),b),
        Math.min(LevenshteinDistance(a,b.substring(1)),
        LevenshteinDistance(a.substring(1),b.substring(1))));
    }

    // private float Jaro_Wrinkler(String a, String b){

    // }

    public static void main(String[] args){
        WebScraper ws = new TakealotScraper();
        ws.scrape("Iphone 16");
    }
}
