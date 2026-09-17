package com.boardwise.backend.shared.repository;

import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.shared.model.Boardgame;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardGameSearch {

    private final MongoTemplate template;

    public List<Boardgame> search(String query, int limit){
        
        Document fuzzy = new Document()
                        .append("maxEdits", 1)
                        .append("prefixLength", 2)
                        .append("maxExpansions", 25);
        
        Document stage = new Document("$search", new Document("index", "boardgame_search")
        .append("autocomplete", new Document()
                .append("query", query)
                .append("path", "title")
                .append("fuzzy", fuzzy)
        ));

        Aggregation agg = Aggregation.newAggregation(
            context -> stage,
            Aggregation.project("id", "title")
                    .andExpression("meta('searchScore')").as("score"),
            Aggregation.sort(Sort.Direction.DESC, "score"),
            Aggregation.limit(limit)
        );

        return template.aggregate(agg, "BOARD_GAME", Boardgame.class).getMappedResults();
    }
}
