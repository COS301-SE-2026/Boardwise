package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.enums.LiveEventAttendeeStatus;

public record LiveEventStatusRequestDTO(LiveEventAttendeeStatus status) {}