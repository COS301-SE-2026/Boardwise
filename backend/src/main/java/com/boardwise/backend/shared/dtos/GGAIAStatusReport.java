package com.boardwise.backend.shared.dtos;

public record GGAIAStatusReport(
    String userId,
    String status,
    String reason
) {}
