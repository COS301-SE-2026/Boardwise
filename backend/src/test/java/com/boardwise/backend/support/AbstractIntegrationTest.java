package com.boardwise.backend.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;



@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class AbstractIntegrationTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");
    
    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry){
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
}
