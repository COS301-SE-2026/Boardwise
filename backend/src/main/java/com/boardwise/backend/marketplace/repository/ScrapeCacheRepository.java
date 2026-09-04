package com.boardwise.backend.marketplace.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.marketplace.models.ScrapeCache;

public interface ScrapeCacheRepository extends MongoRepository<ScrapeCache, String> {
    Optional<ScrapeCache> findBySearchTerm(String searchTerm);
       List<ScrapeCache> findByLastScrapedAtAfterOrderByLastScrapedAtDesc(LocalDateTime cutoff, Limit limit);
}