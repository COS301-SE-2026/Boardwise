package com.boardwise.backend;

import java.util.List;

import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.SearchIndexModel;
import com.mongodb.client.model.SearchIndexType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchIndexInitializer implements CommandLineRunner{

    private final MongoTemplate template;

    @Override
    public void run(String... args){
        String collectionName = "BOARD_GAME";
        MongoCollection<Document> collection = template.getCollection(collectionName);

        if(!template.collectionExists(collectionName)){
            template.createCollection(collectionName);
        }

        boolean exists = false;
        for(Document index : collection.listSearchIndexes()){
            if("boardgame_search".equals(index.getString("name"))){
                exists = true;
                break;
            }
        }

        if(!exists){
            Document def = new Document("mappings",
                new Document("dynamic", false).append(
                    "fields", new Document(
                        "title", 
                        List.of(
                            new Document("type", "string")
                                .append("analyzer", "lucene.standard"),
                            new Document("type", "autocomplete")
                                .append("tokenization", "edgeGram")
                                .append("minGrams", 2)
                                .append("maxGrams", 15)
                                .append("foldDiacritics", false)
                        )
                    )
                )
            );

            SearchIndexModel model = new SearchIndexModel(
                "boardgame_search",
                def,
                SearchIndexType.search()
            );
            collection.createSearchIndexes(List.of(model));
            System.out.println("Fuzzy Search index created. Name: 'boardgame_search'");
        }
    }
}
