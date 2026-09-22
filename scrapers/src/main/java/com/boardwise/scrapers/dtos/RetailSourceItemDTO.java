package com.boardwise.scrapers.dtos;

import com.boardwise.scrapers.enums.MatchType;

import lombok.Builder;

@Builder
public record RetailSourceItemDTO(
        String retailTitle,
        String retailer,
        String url,// site based URL
        Double price,
        String imageUrl,
        MatchType matchType
){
        public RetailSourceItemDTO withMatchType(MatchType type) {
                return new RetailSourceItemDTO(retailTitle, retailer, url, price, imageUrl, type);
        }
}