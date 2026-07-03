package com.boardwise.backend.vault.repository;

import java.time.Instant;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.vault.model.Rulebook;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RulebookRepositoryCustomImpl implements RulebookRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public Rulebook atomicAcquireWriteLock(String rulebookId, String userId, Instant newExpiry){
        Instant now = Instant.now();

        // Construct query criteria
        Criteria lockAvailablCriteria = new Criteria().orOperator(
            Criteria.where("lockHeldBy").isNull(),
            Criteria.where("lockExpiresAt").lt(now)
        );

        // Construct the query
        Query query = new Query(
            new Criteria().andOperator(
                Criteria.where("_id").is(rulebookId),
                lockAvailablCriteria
            )
        );

        // Construct the update definition
        Update update = new Update()
            .set("lockHeldBy", userId)
            .set("lockExpiresAt", newExpiry);

        // Get the updated document
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        // Execute atomic command
        return mongoTemplate.findAndModify(query, update, options, Rulebook.class);
    }
}
