package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.enums.LiveEventAttendeeStatus;

public record LiveEventAttendee(String userId, String username, LiveEventAttendeeStatus status, boolean isHost){}
