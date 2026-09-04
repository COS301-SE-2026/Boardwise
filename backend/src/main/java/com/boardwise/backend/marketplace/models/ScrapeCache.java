package com.boardwise.backend.marketplace.models;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Document(collection = "SCRAPED_CACHE")
@Data
@Builder
@AllArgsConstructor
public class ScrapeCache {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("searchTerm")
    private String searchTerm;

    @Field("results")
    private List<RetailSourceItemDTO> results;
    
    @Field("lastScrapedAt")
    private LocalDateTime lastScrapedAt;
}
