package com.boardwise.backend.vault.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.boardwise.backend.vault.model.RulebookText;

public interface RulebookTextRepository extends MongoRepository<RulebookText, ObjectId>, RulebookTextRepositoryCustom {
    Optional<RulebookText> findByRulebookId(ObjectId rulebookId);

    @Query(value = "{'rulebookId': ?0, 'chunks.chunkId': {$oid: ?1}}", fields = "{'chunks.$': 1}")
    Optional<RulebookText> findBySpecificChunk(ObjectId rulebookId, String chunkId);
}
