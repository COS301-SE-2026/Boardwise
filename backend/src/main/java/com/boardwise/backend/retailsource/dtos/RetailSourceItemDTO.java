package com.boardwise.backend.retailsource.dtos;

import java.util.List;
import lombok.Builder;

@Builder
public record RetailSourceItemDTO(
        String gameIitle,
        String retailTitle,
        Double price,
        String description,
        String imageUrl)
        {}