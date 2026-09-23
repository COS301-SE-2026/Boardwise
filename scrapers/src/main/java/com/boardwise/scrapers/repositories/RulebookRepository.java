package com.boardwise.scrapers.repositories;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.scrapers.models.Rulebook;

public interface RulebookRepository extends MongoRepository<Rulebook, ObjectId>{
    boolean existsByGameIdAndStatusNot(ObjectId gameId, String status);
    
    List<Rulebook> findByStatusNot(String status);
    List<Rulebook> findByGameId(ObjectId gameId);
}