package com.boardwise.backend.marketplace.dtos.retailsource;

import lombok.Builder;

@Builder
public record RetailSourceItemDTO(
        String retailTitle,
        String retailer,
        String url,// site based URL
        Double price,
        String imageUrl, // image
        float JaroWinklerSimilarityScore){}