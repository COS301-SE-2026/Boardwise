package com.boardwise.scrapers.dtos;

import java.util.List;

public record BoardgamesRequest(List<String> titles, int page, int size) {}