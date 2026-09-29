package com.boardwise.backend.shared.dtos;

import lombok.Builder;

@Builder 
public record OnboardingDTO(
    String id,
    String title,
    String imageUrl
) {}
