package com.boardwise.backend.user_service.repos;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.user_service.models.Boardgame;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardGameSearch {

    private final MongoTemplate template;

    public List<Boardgame> search(String query, int limit){
        Document stage = new Document("$search", new Document("index", "boardgame_search").append(
            "text", 
            new Document()
                .append("query", query)
                .append("path", "title")
                .append("fuzzy", new Document()
                                .append("maxEdits", 2)
                                .append("prefixLength", 1)
                )
        ));

        Aggregation agg = Aggregation.newAggregation(
            context -> stage,
            Aggregation.limit(limit)
        );

        return template.aggregate(agg, "BOARD_GAME", Boardgame.class).getMappedResults();
    }
}
