package com.boardwise.backend.scraper.dtos;

import java.util.List;
public record ScrapeResponse(
    String site, 
    List<String> links){}
