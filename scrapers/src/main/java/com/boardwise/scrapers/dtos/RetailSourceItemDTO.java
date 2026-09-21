package com.boardwise.scrapers.dtos;

import lombok.Builder;

@Builder
public record RetailSourceItemDTO(
        String retailTitle,
        String retailer,
        String url,// site based URL
        Double price,
        String imageUrl
){}