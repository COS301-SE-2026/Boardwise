package com.boardwise.scrapers.dtos;

public record TableTopGuruResDTO(
    String retailTitle,
    String retailer,
    String url,
    boolean isAvailable,
    boolean isOnSale,
    Double originalPrice,
    Double salePrice,
    String imageUrl
) {
} 
