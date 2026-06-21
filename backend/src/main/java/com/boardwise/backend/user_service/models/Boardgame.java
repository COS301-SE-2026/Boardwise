package com.boardwise.backend.user_service.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "BOARD_GAMES")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Boardgame {
    @Id
    private String id;
    private String title;
    private String description;
    @Field("image_url")
    private String imageURL;
    private List<String> genres;
}
