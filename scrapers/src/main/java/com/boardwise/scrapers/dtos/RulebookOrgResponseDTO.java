package com.boardwise.scrapers.dtos;

import java.util.List;

public record RulebookOrgResponseDTO(
        List<RulebookOrgItemDTO> results
) {}