package com.boardwise.backend;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.testcontainers.containers.MongoDBContainer;

public abstract class SharedMongoContainer {
    @ServiceConnection
    protected static final MongoDBContainer MONGO_CONTAINER;

    static{
        MONGO_CONTAINER = new MongoDBContainer("mongo:7.0");
        MONGO_CONTAINER.start();
    }

    @Autowired
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void resetMongoDatabase(){
        mongoTemplate.getCollectionNames().forEach(c -> mongoTemplate.remove(new Query(), c));
    }
}
