package com.boardwise.backend.user_service.dtos.request;

import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.shared.dtos.OtherGameDTO;

public record BulkOtherGameDto(
    OtherGameDTO gameInfo,
    MultipartFile gameImage
) {}