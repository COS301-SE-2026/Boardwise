package com.boardwise.backend.vault.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.vault.model.Rulebook;

public interface RulebookRepository extends MongoRepository<Rulebook, ObjectId>, RulebookRepositoryCustom {
    Page<Rulebook> findByStatusAndTitleContainingIgnoreCase(
        String status, String title, Pageable pageable
    );

    List<Rulebook> findByLockHeldBy(ObjectId userId);
}
