package com.boardwise.backend.user_service.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Users")
public class User {
    
    @Id
    private Integer id;
}
