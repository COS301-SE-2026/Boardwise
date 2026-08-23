package com.boardwise.backend.marketplace.service.webscrapers;
import java.util.List;

import org.springframework.stereotype.Service;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;

//Interface class 
@Service
public interface WebScraper {
    
    public List<RetailSourceItemDTO> scrape(String toSearch);    
    public final float STRINGMATCH = 0.65f; // >=65% string match gets Returned
    
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

    default float JaroWinklerSimilarity(String toSearch, String comp){
        final float prefixScale = 0.25f;  
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
