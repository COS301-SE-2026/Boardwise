package com.boardwise.scrapers.dtos;

public record TimelessBoardGamesResDTO(
        String retailTitle,
        String retailer,
        String url,// site based URL
        Boolean isAvailable,
        Boolean isOnSale,
        Double originalPrice,
        Double salePrice, // 
        String imageUrl // image
){}