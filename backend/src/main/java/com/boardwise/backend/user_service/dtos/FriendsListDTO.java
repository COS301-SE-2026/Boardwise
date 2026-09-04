package com.boardwise.backend.user_service.dtos;

import java.util.List;

public record FriendsListDTO(
    String message,
    List<FriendDTO> friends,
    List<FriendDTO> mutuals
) {}
