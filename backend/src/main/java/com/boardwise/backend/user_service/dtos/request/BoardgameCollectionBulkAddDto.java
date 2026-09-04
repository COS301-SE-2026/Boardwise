package com.boardwise.backend.user_service.dtos.request;

import java.util.List;

public record BoardgameCollectionBulkAddDto(List<String> knownGameIds){}
