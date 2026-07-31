package com.boardwise.backend.vault.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.vault.model.EditEvent;

public interface EditEventRepository extends MongoRepository<EditEvent, ObjectId>{
    List<EditEvent> findByRulebookIdOrderByCommittedAtAsc(ObjectId rulebookId);

    Optional<EditEvent> findByRulebookIdAndVersionPostEdit(ObjectId rulebookId, long versionPostEdit);
}
