package com.boardwise.backend.user_service.dtos.response;

import java.util.List;

import com.boardwise.backend.user_service.dtos.ConversationDTO;

public record ConversationsResponseDTO(
    String message,
    List<ConversationDTO> conversations
) {}
