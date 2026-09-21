package com.boardwise.scrapers.dtos;

public record RetailSourceItemDTO(
        String retailTitle,
        String retailer,
        String url,// site based URL
        Double price,
        String imageUrl, // image
        float JaroWinklerSimilarityScore
){}