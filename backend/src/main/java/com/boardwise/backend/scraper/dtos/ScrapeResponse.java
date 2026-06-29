package com.boardwise.backend.scraper.dtos;

import java.util.List;

import lombok.Builder;
import java.util.AbstractMap;

@Builder
public record ScrapeResponse(
    String site, 
    AbstractMap.SimpleEntry<String, Float> details
){}