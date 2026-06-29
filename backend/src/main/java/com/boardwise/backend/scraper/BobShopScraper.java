package com.boardwise.backend.scraper;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.boardwise.backend.scraper.dtos.ScrapeResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class BobShopScraper implements WebScraper{
   
    public BobShopScraper(){} 
    
    private float stringMatch = 0.5f; // >=50% string match gets Returned
    private float prefixScale = 0.2f;  
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

}
