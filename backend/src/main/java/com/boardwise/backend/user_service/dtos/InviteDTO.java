package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.enums.RSVPStatus;

public record InviteDTO(
    RSVPStatus status,
    EventHostInfo host,
    EventInviteInfo event
) {}
