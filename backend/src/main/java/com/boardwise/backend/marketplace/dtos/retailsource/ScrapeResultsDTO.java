package com.boardwise.backend.marketplace.dtos.retailsource;

import java.util.List;

public record ScrapeResultsDTO(
        List<RetailSourceItemDTO> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean last,
        boolean first,
        int numberOfElements,
        boolean empty
) {}