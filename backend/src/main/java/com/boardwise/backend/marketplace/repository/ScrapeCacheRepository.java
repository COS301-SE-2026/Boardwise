package com.boardwise.backend.marketplace.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.marketplace.models.ScrapeCache;

public interface ScrapeCacheRepository extends MongoRepository<ScrapeCache, String> {
    Optional<ScrapeCache> findBySearchTerm(String searchTerm);
}