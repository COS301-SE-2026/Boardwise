package com.boardwise.backend.user_service.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.enums.EventPrivacy;
import com.boardwise.backend.user_service.models.LiveEvent;

public interface LiveEventRepository extends MongoRepository<LiveEvent, String> {
    List<LiveEvent> findByPrivacyAndDateGreaterThanEqual(EventPrivacy privacy, LocalDate date);
}
