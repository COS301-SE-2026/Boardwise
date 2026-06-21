package com.boardwise.backend.user_service.repos;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.Boardgame;

public interface BoardGameRepository extends MongoRepository<Boardgame, String>{
    Optional<Boardgame> findByTitle(String title);

    Optional<Boardgame> findTopByBggIdNotNullOrderByBggIdDesc();
}
