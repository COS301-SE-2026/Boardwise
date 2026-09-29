package com.boardwise.backend.marketplace.dtos.retailsource;

import java.util.List;

public record BoardgamesRequest(List<String> titles, int page, int size) {}