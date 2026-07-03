package com.boardwise.backend.vault.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.vault.model.RulebookText;

public interface RulebookTextRepository extends MongoRepository<RulebookText, ObjectId>, RulebookTextRepositoryCustom {
    Optional<RulebookText> findByRulebookId(ObjectId rulebookId);
}
