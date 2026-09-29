
package com.boardwise.backend.shared.dtos;

import java.util.List;

import lombok.Builder;

@Builder 
public record GenreRequestDTO(List<String> genres) {}