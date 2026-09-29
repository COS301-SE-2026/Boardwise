package com.boardwise.scrapers.dtos;


import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import jakarta.validation.constraints.NotNull;

public record RuleBookReqResponse(
       @NotNull @JsonSerialize(using = ToStringSerializer.class) ObjectId id
) {}
