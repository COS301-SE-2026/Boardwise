package com.boardwise.backend.vault.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.vault.model.Rulebook;

@Repository
public interface RulebookRepository extends MongoRepository<Rulebook, ObjectId> {
    Page<Rulebook> findByStatusAndGameNameContainingIgnoreCase(
        String status, String gameName, Pageable pageable
    );
}
