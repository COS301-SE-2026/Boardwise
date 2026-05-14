package com.boardwise.backend.vault.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.vault.model.WriteLock;

@Repository
public interface WriteLockRepository extends MongoRepository<WriteLock, ObjectId> {
    Optional<WriteLock> findByRulebookId(ObjectId rulebookId);

    List<WriteLock> findByExpiresAtBefore(Instant now);
}
