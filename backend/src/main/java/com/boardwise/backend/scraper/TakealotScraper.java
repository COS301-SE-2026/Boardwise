package com.boardwise.backend.scraper;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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

    private float stringMatch = 0.4f; // >=40% string match gets Returned
    float prefixScale = 0.1f;  
    private String searchSelector = "input[placeholder='Search for products, brands...']";
    private Map<Integer,String> map = new HashMap<>(); 
    private final String site = "https://www.takealot.com";

    public List<ScrapeResponse> scrape(String toSearch) {
        System.out.println("Searching for..."+toSearch);
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
            String tempComp = "FARMVILLE";
            boolean accept = acceptLevenshteinDistance(toSearch, tempComp);

            // Jaro-Winkler - similarity between 2 sequences
            boolean accept_2 = acceptJaroWinklerSimilarity(toSearch, tempComp);

            Files.write(testPdf, page.pdf());
            page.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private float LevenshteinDistance(String a, String b){
        int[] prev = new int[b.length()+1];
        int[] curr = new int[b.length()+1];

        for (int j = 0; j <= b.length(); j++) prev[j] = j; // placeholder

        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i; 
            for (int j = 1; j <= b.length(); j++) {
                int match = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j-1] +1, prev[j]+1), prev[j-1] + match);
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[b.length()];    
    }

    private boolean acceptLevenshteinDistance(String toSearch, String b){
            float LevDist = LevenshteinDistance(toSearch.toLowerCase(), b.toLowerCase()); // for inc accuracy purposes: remove spaces & compare numbers
            float percOfNeg = LevDist/(float)toSearch.length();
            float res = 1 - percOfNeg;
            System.out.println("Levenshtein Distance: "+res +"%" + " vs " + stringMatch+"%");
            return res >= stringMatch;
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
        System.out.println("Result in JW : " + result);
        return result +(prefixScale* (1- result));
    }

    private boolean acceptJaroWinklerSimilarity(String toSearch, String toComp){        
        float res = JaroWinklerSimilarity(toSearch.toLowerCase(), toComp.toLowerCase());
         return res >= stringMatch;
    }
    public static void main(String[] args){
        WebScraper ws = new TakealotScraper();
        ws.scrape("FAREMVIEL");
    }
}