package com.boardwise.backend.user_service.dtos.response;

import com.boardwise.backend.user_service.enums.ResponseStatus;

public record GroupInviteResponse(
    ResponseStatus status
) {}
