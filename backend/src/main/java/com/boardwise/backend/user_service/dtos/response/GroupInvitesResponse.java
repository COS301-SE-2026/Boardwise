package com.boardwise.backend.user_service.dtos.response;

import java.util.List;

import com.boardwise.backend.user_service.dtos.GroupInviteInfo;

public record GroupInvitesResponse(
    String message,
    List<GroupInviteInfo> invites
) {}
