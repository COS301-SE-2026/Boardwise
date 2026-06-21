package com.boardwise.backend.user_service.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "BOARD_GAMES")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Boardgame {
    @Id
    private String id;
    @Indexed(unique = true)
    private int bggId;
    private String title;
    private String description;
    private String imageURL;
    private List<String> genres;
}
