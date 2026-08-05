package com.boardwise.backend.user_service.dtos;

import java.time.LocalDate;

public record EventInviteInfo(
    String id,
    String name,
    String image,
    LocalDate date
) {}
