package com.boardwise.backend.vault.repository;


import java.time.Instant;
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
        Criteria updateCriteria = new Criteria().andOperator(
            Criteria.where("rulebookId").is(rulebookId),
            Criteria.where("chunkId").is(chunkId)
        );

        Query query = new Query(updateCriteria);

        Update update = new Update()
            .set("content", newContent)
            .set("charCount", newContent.length())
            .set("updatedAt", Instant.now());

        mongoTemplate.updateFirst(query, update, RulebookText.class);
    }

    @Override
    public RulebookText atomicInsertChunk(ObjectId rulebookId, String content, int insertIndex){
        // Fetch the total number of chunks so that the insertIndex is bounded within
        long totalChunks = mongoTemplate.count(
            new Query(Criteria.where("rulebookId").is(rulebookId)),
                RulebookText.class
        );

        int actualIndex = (insertIndex >= 0 && insertIndex <= totalChunks) ? insertIndex : (int)totalChunks;
        
        Criteria shiftCriteria = new Criteria().andOperator(
            Criteria.where("rulebookId").is(rulebookId),
            Criteria.where("index").gte(actualIndex)
        );
        Query query = new Query(shiftCriteria);
        Update update = new Update().inc("index", 1);

        mongoTemplate.updateMulti(query, update, RulebookText.class);

        RulebookText newChunk = RulebookText.builder()
            .rulebookId(rulebookId)
            .chunkId(new ObjectId())
            .index(actualIndex)
            .content(content)
            .embedding(null) // AI pipeline will fill this asynchronously later
            .charCount(content.length())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        
        mongoTemplate.insert(newChunk);

        return newChunk;
    }

    @Override
    public boolean atomicDeleteChunk(ObjectId rulebookId, ObjectId chunkId){
        Criteria deleteCriteria = new Criteria().andOperator(
            Criteria.where("rulebookId").is(rulebookId),
            Criteria.where("chunkId").is(chunkId)
        );
        Query query = new Query(deleteCriteria);

        RulebookText removedChunk = mongoTemplate.findAndRemove(query, RulebookText.class);

        if(removedChunk == null){
            return false;
        }

        Criteria shiftCriteria = new Criteria().andOperator(
            Criteria.where("rulebookId").is(rulebookId),
            Criteria.where("index").gt(removedChunk.getIndex()));
        Query updateQuery = new Query(shiftCriteria);
        Update update = new Update().inc("index", -1);

        mongoTemplate.updateMulti(updateQuery, update, RulebookText.class);

        return true;
    }
}
