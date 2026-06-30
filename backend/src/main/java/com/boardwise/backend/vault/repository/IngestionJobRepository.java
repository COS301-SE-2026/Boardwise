package com.boardwise.backend.vault.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.vault.model.IngestionJob;

public interface IngestionJobRepository extends MongoRepository<IngestionJob, ObjectId> {
    Optional<IngestionJob> findByRulebookId(ObjectId rulebookId);
}
