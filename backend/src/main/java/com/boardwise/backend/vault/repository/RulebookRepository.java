package com.boardwise.backend.vault.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.vault.model.Rulebook;

public interface RulebookRepository extends MongoRepository<Rulebook, ObjectId>, RulebookRepositoryCustom {
    List<Rulebook> findByLockHeldBy(ObjectId userId);
}
