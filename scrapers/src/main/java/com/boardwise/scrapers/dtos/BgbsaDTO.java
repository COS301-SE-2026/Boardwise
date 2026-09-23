package com.boardwise.scrapers.dtos;

public record BgbsaDTO(
        String retailerTitle,
        String retailer,
        Double originalPrice,
        boolean isOnSale,
        Double salePrice,
        String url,
        String imageUrl
) {
 
}
