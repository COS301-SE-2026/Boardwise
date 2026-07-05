package com.boardwise.backend.vault.repository;


import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.aggregation.VariableOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.vault.model.RulebookText;
import com.mongodb.client.result.UpdateResult;

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

    @Override
    public boolean atomicInsertChunk(ObjectId rulebookId, ObjectId chunkId, String content, int insertIndex, int lastIndex){
        Query query = new Query(Criteria.where("rulebookId").is(rulebookId));

        if (insertIndex >= 0 && insertIndex <= lastIndex){
            Document newChunk = new Document()
                .append("chunkId", chunkId)
                .append("index", insertIndex)
                .append("content", content);

            AggregationUpdate updatePipeline = AggregationUpdate.update()
                .set("chunks").toValue( // Slice, insert and append
                    new Document("$concatArrays", List.of(
                        new Document("$slice", List.of(
                            new Document("$ifNull", List.of("$chunks", List.of())),
                            0,
                            insertIndex
                        )),
                        List.of(newChunk),
                        new Document("$slice", List.of(
                            new Document("$ifNull", List.of("$chunks", List.of())),
                            insertIndex,
                            new Document("$size", new Document("$ifNull", List.of("$chunks", List.of())))
                        ))
                    ))
                )
                .set("chunks").toValue(
                    new Document("$map", new Document()
                        .append("input", new Document("$range", List.of(0, new Document("$size", "$chunks"))))
                        .append("as", "idx")
                        .append("in", new Document("$mergeObjects", List.of(
                            new Document("$arrayElemAt", List.of("$chunks", "$$idx")),
                            new Document("index", "$$idx")
                        )))
                    )
                );
                UpdateResult result = mongoTemplate.updateFirst(query, updatePipeline, RulebookText.class);
                return result.getModifiedCount() > 0;
        }else{
            Document newChunk = new Document()
            .append("chunkId", chunkId)
            .append("index", lastIndex + 1)
            .append("content", content);

            Update update = new Update().push("chunks", newChunk);

            UpdateResult result = mongoTemplate.updateFirst(query, update, RulebookText.class);

            return result.getModifiedCount() > 0;
        }
    }
}
