<<<<<<<< HEAD:backend/src/main/java/com/boardwise/backend/user_service/repository/BoardGameSearch.java
package com.boardwise.backend.user_service.repository;
========
package com.boardwise.backend.shared.repository;
>>>>>>>> 8f4054c6958c66fbb30aeb0c75014dee8a3cea30:backend/src/main/java/com/boardwise/backend/shared/repository/BoardGameSearch.java

import java.util.List;

import org.bson.Document;
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
        Document stage = new Document("$search", new Document("index", "boardgame_search")
        .append("autocomplete", new Document()
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
