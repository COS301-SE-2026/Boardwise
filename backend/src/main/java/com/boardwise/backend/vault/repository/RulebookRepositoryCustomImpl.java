package com.boardwise.backend.vault.repository;

import java.time.Instant;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
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
    public Rulebook atomicAcquireWriteLock(ObjectId rulebookId, ObjectId userId, Instant newExpiry){
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

    @Override
    public Rulebook atomicValidateAndExtendLock(ObjectId rulebookId, ObjectId userId, long expectedVersion, Instant newExpiry){
        Query query = new Query(
            new Criteria().andOperator(
                Criteria.where("_id").is(rulebookId),
                Criteria.where("lockHeldBy").is(userId),
                Criteria.where("version").is(expectedVersion)
            )
        );

        Update update = new Update()
            .inc("version", 1)
            .set("lockExpiresAt", newExpiry)
            .set("updatedAt", Instant.now());

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        return mongoTemplate.findAndModify(query, update, options, Rulebook.class);
    }

    @Override
    public Rulebook atomicReleaseWriteLock(ObjectId rulebookId, ObjectId userId){
        Query query = new Query(
            new Criteria().andOperator(
                Criteria.where("_id").is(rulebookId),
                Criteria.where("lockHeldBy").is(userId)
            )
        );

        Update update = new Update()
            .set("lockHeldBy", null)
            .set("lockExpiresAt", null);

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        return mongoTemplate.findAndModify(query, update, options, Rulebook.class);
    }

    @Override
    public void atomicReleaseAllWriteLocks(ObjectId userId){
        Query query = new Query(Criteria.where("lockHeldBy").is(userId));

        Update update = new Update()
            .set("lockHeldBy", null)
            .set("lockExpiresAt", null);
        
        mongoTemplate.updateMulti(query, update, Rulebook.class);
    }

    @Override
    public Long atomicPopUndoAndPushRedo(ObjectId rulebookId, ObjectId userId){
        Query query = new Query(
            Criteria.where("_id").is(rulebookId)
            .and("lockHeldBy").is(userId)
            .and("undoStack.0").exists(true)
        );

        AggregationUpdate updatePipeline = AggregationUpdate.update()
        .set("redoStack").toValue(
            new Document("$concatArrays", List.of(
                new Document("$ifNull", List.of("$redoStack", List.of())),
                List.of(new Document("$arrayElemAt", List.of("$undoStack", -1)))
            ))
        )
        .set("undoStack").toValue(
            new Document("$slice", List.of(
                "$undoStack",
                0,
                new Document("$subtract", List.of(new Document("$size", "$undoStack"), 1))
            ))
        );

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        Rulebook updatedRulebook = mongoTemplate.findAndModify(query, updatePipeline, options, Rulebook.class);

        if(updatedRulebook == null){
            return null;
        }

        List<Long> redoStack = updatedRulebook.getRedoStack();
        return redoStack.get(redoStack.size() - 1);
    }

    @Override
    public void atomicCommitForwardEdit(ObjectId rulebookId, Long newVersion){
        Query query = new Query(Criteria.where("_id").is(rulebookId));

        AggregationUpdate updatePipeline = AggregationUpdate.update()
            .set("undoStack").toValue( // Push new version onto undo stack
                new Document("$slice", List.of(
                    new Document("$concatArrays", List.of(
                        new Document("$ifNull", List.of("$undoStack", List.of())),
                        List.of(newVersion)
                    )),
                    -50 // Dynamically keep only the last 50 elements
                ))
            )
            .set("redoStack").toValue(List.of()); // clear redo stack

        mongoTemplate.updateFirst(query, updatePipeline, Rulebook.class);
    }
}
