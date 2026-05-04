package com.boardwise.backend.vault.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.vault.model.EditEvent;

@Repository
public interface EditEventRepository extends MongoRepository<EditEvent, ObjectId>{
    List<EditEvent> findByRulebookIdOrderByCommittedAtAsc(ObjectId rulebookId);
}
