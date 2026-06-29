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
    //DEBUG LINE : DELETE EVERYTHING UNDER
    Path fileToDelete = Path.of("backend/src/main/java/com/boardwise/backend/scraper/deleteMe.pdf");

    //END OF DEBUG
    private float stringMatch = 0.25f; // >=30% string match gets Returned
    private float prefixScale = 0.5f;  
    private final int MAXNUMITEMS = 15;
    private String searchSelector = "input[placeholder='The search for fun starts here...']";
    private final String site = "https://www.toysrus.co.za/";

    public List<ScrapeResponse> scrape(String toSearch) {
        System.out.println("Searching for... \"" + toSearch + "\"");
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

            Files.write(fileToDelete,page.pdf());

            //find search bar
            Locator searchBar = page.getByPlaceholder("The search for fun starts here...");

            if(searchBar.count() == 0){
                throw new RuntimeException("Error while trying to find the search bar on Takealot");
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

            for(ScrapeResponse x: matching){
                System.out.println(x.details().getKey());
            }

            return matching;

        } catch (Exception e) {
            e.printStackTrace();
        }
        //if you ever exit this... wow
        throw new RuntimeException("somehow reached a place you shouldn't have ");
    }

    private int countMatchingChars(String x, String y){
        if(x.isBlank()|| y.isBlank()){
            throw new RuntimeException("Cannot pass in empty strings");
        }
        int lenOfX = x.length();
        int lenOfY = y.length();
        int matchWindow = Math.max(1, Math.max(lenOfX, lenOfY)/2-1);
        boolean[] yMatches = new boolean[lenOfY];
        int count = 0;
        for (int i = 0; i < lenOfX; i++) {
            int start = Math.max(0,i-matchWindow);
            int end = Math.min(lenOfY,i+matchWindow+1);

            for (int j = start; j < end; j++) {
                if (!yMatches[j] && x.charAt(i) == y.charAt(j)) {
                    yMatches[j] = true;
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private int countTranspositions(String x, String y) {
        if (x == null || y == null || x.isBlank() || y.isBlank()) {
            throw new IllegalArgumentException("Cannot pass in empty or null strings");
        }

        int lenOfX = x.length();
        int lenOfY = y.length();

        //match window limit
        int matchWindow = Math.max(1, Math.max(lenOfX, lenOfY) / 2 - 1);

        boolean[] xMatches = new boolean[lenOfX];
        boolean[] yMatches = new boolean[lenOfY];
        int matches = 0;

        // Find matches
        for (int i = 0; i < lenOfX; i++) {
            int start = Math.max(0, i - matchWindow);
            int end = Math.min(lenOfY, i + matchWindow + 1);

            for (int j = start; j < end; j++) {
                if (!yMatches[j] && x.charAt(i) == y.charAt(j)) {
                    xMatches[i] = true;
                    yMatches[j] = true;
                    matches++;
                    break;
                }
            }
        }

        // no matches
        if (matches == 0) return 0;

        //Compare matched characters to count mismatches
        int mismatches = 0;
        int yIdx =0;

        for (int i =0; i < lenOfX; i++) {
            if (xMatches[i]) {
                //while pos don't match
                while (!yMatches[yIdx]) {
                    yIdx++;
                }
                //mismatch
                if (x.charAt(i) != y.charAt(yIdx)) {
                    mismatches++;
                }
                yIdx++;
            }
        }
        return mismatches/2;
    }

    private float JaroSimilarity(String a, String b){
        if(a.isBlank()|| b.isBlank()){
            throw new RuntimeException("Cannot pass in empty strings");
        }
        int m = countMatchingChars(a,b);
        int t = countTranspositions(a,b);

        if(m == 0) return 0;
        
        float Comp1 = (float) m / a.length();
        float Comp2 = (float) m / b.length();
        float Comp3 = (float) (m - t) / m;

        return (1f/3f) * (Comp1 +Comp2 + Comp3);
    }

    private float JaroWinklerSimilarity(String toSearch, String comp){
        float result = JaroSimilarity(toSearch, comp);
        int l = commonPrefixLength(toSearch, comp);//common prefix len
        return result +(l* prefixScale * (1-result));
    }

    private int commonPrefixLength(String a, String b) {
        int max = Math.min(4, Math.min(a.length(), b.length()));
        int i = 0;
        while (i < max && a.charAt(i) == b.charAt(i)) i++;
        return i;
    }

    public static void main(String[] args){
        WebScraper ws = new ToysRUsScraper();
        ws.scrape("Monopoly");
    }
}