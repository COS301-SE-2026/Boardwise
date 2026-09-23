package com.boardwise.scrapers.services.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class TitleRelevanceFilter {
    private static final Set<String> STOP_WORDS= Set.of("the", "a", "an", "of", "game", "edition","boardgame", "&", "and");

    private TitleRelevanceFilter(){}

    public static boolean isExactMatch(String query, String title){
        Set<String> queryTokens = tokenise(query);
        if (queryTokens.isEmpty()) {
                Set<String> rawQueryTokens = tokeniseRaw(query);
                if (rawQueryTokens.isEmpty()) return true; // query was truly empty/blank
                return tokeniseRaw(title).containsAll(rawQueryTokens);
            }  
            return tokenise(title).containsAll(queryTokens);
    }

    private static Set<String> tokeniseRaw(String text){
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKD)
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ");

        return Arrays.stream(normalized.split("\\s+"))
            .filter(word -> !word.isBlank())
            .collect(Collectors.toSet()); 
    }
    public static double relatedScoreUsingJaccaradSimilarity(String query, String title){
        Set<String> queryTokens = tokenise(query);
        Set<String> titleTokens = tokenise(title);

        if(queryTokens.isEmpty() || titleTokens.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(queryTokens);
        intersection.retainAll(titleTokens);

        Set<String> union = new HashSet<>(queryTokens);
        union.addAll(titleTokens);

        return (double) intersection.size() / union.size();
    }
    public static boolean isRelevant(String query, String title){
        Set<String> queryTokens = tokenise(query);

        Set<String> titleTokens = tokenise(title);

        return titleTokens.containsAll(queryTokens);
    }

    private static Set<String> tokenise(String text){

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKD)
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " "); // strip punctuation

            return Arrays.stream(normalized.split("\\s+"))
            .filter(word -> !word.isBlank())
            .filter(word -> !STOP_WORDS.contains(word))
            .collect(Collectors.toSet());

    }
}


