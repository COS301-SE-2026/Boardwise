package com.boardwise.scrapers.dtos;

import java.util.List;
import java.util.Map;

public record ScrapeResultsDTO(Map<String, List<RetailSourceItemDTO>> results, List<String> boardgames) {
    
}
