package com.boardwise.backend.user_service.dtos;

import java.time.LocalDate;

public record EventInviteInfo(
    String eventId,
    String eventName,
    String eventImg,
    LocalDate eventDate
) {}
