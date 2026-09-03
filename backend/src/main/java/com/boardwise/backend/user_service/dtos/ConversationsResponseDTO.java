package com.boardwise.backend.user_service.dtos;

import java.util.List;

public record ConversationsResponseDTO(
    String message,
    List<ConversationDTO> conversations
) {}
