package com.boardwise.backend.vault.repository;


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
    public RulebookText atomicInsertChunk(ObjectId rulebookId, ObjectId chunkId, String content, int insertIndex){
        Query query = new Query(Criteria.where("rulebookId").is(rulebookId));

        Document newChunk = new Document()
                .append("chunkId", (chunkId != null) ? chunkId : new ObjectId())
                .append("content", content);

        Document arraySize = new Document("$size", new Document("$ifNull", List.of("$chunks", List.of())));

        Document safeIndex = new Document("$cond", List.of(
            new Document("$or", List.of(
                new Document("$lt", List.of(insertIndex, 0)),
                new Document("$gt", List.of(insertIndex, arraySize))
            )),
            arraySize, // Fallback: Append to end
            insertIndex // Default: Insert into requested index
        ));

        AggregationUpdate updatePipeline = AggregationUpdate.update()
            .set("chunks").toValue( // Slice, insert and append
                new Document("$let", new Document()
                    .append("vars", new Document("safeIdx", safeIndex))
                    .append("in", new Document("$concatArrays", List.of(
                        new Document("$slice", List.of(
                            new Document("$ifNull", List.of("$chunks", List.of())),
                            0,
                            "$$safeIdx"
                        )),
                        List.of(newChunk),
                        new Document("$slice", List.of(
                            new Document("$ifNull", List.of("$chunks", List.of())),
                            "$$safeIdx",
                            arraySize
                        ))
                    )))
                )
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

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        return mongoTemplate.findAndModify(query, updatePipeline, options,RulebookText.class);
    }

    @Override
    public boolean atomicDeleteChunk(ObjectId rulebookId, ObjectId chunkId){
        Query query = new Query(Criteria.where("rulebookId").is(rulebookId));

        AggregationUpdate updatePipeline = AggregationUpdate.update()
            .set("chunks").toValue(
                new Document("$filter", new Document("input", new Document("$ifNull", List.of("$chunks", List.of())))
                    .append("as", "chunk")
                    .append("cond", new Document("$ne", List.of("$$chunk.chunkId", chunkId)))
                )
            )
            .set("chunks").toValue(
                new Document("$map", new Document()
                    .append("input", new Document("$range", List.of(0, new Document("$size", "$chunks"))))
                    .append("as", "idx")
                    .append("in", new Document("$mergeObjects", List.of(
                        new Document("$arrayElemAt", List.of("$chunks", "$$idx")),
                        new Document("index", "$$idx"))))));

        UpdateResult result = mongoTemplate.updateFirst(query, updatePipeline, RulebookText.class);

        return result.getModifiedCount() > 0;
    }
}
