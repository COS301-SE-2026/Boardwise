package com.boardwise.scrapers.dtos;


import org.bson.types.ObjectId;

import jakarta.validation.constraints.NotNull;

public record RuleBookReqResponse(
    @NotNull ObjectId id
) {}
