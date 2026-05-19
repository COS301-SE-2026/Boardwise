package com.boardwise.backend.user_service.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroupDTO(
    String groupId,
    String name,
    String description,
    String owner,
    int memberCount,
    List<?> members,
    @JsonProperty("isMember")
    boolean isMember
) {}
