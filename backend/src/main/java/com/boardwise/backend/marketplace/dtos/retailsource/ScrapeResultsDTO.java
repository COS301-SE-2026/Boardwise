package com.boardwise.backend.marketplace.dtos.retailsource;

import java.util.List;
import java.util.Map;

public record ScrapeResultsDTO(Map<String, List<RetailSourceItemDTO>> results, List<String> boardgames) {
    
}
