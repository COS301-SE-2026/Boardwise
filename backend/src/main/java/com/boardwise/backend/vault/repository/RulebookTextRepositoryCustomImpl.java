package com.boardwise.backend.vault.repository;


import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.vault.model.RulebookText;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RulebookTextRepositoryCustomImpl implements RulebookTextRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public void atomicUpdateChunk(ObjectId rulebookId, ObjectId chunkId, String newContent){
        Query query = new Query(Criteria.where("rulebookId").is(rulebookId));

        Update update = new Update()
            .set("chunks.$[elem].content", newContent)
            .filterArray(Criteria.where("elem.chunkId").is(chunkId));

        mongoTemplate.updateFirst(query, update, RulebookText.class);
    }
}
