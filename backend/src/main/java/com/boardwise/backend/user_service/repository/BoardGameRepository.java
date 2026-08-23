package com.boardwise.backend.user_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.Boardgame;

public interface BoardGameRepository extends MongoRepository<Boardgame, String>{
    Optional<Boardgame> findByTitle(String title);

    Optional<Boardgame> findTopByBggIdNotNullOrderByBggIdDesc();

    List<Boardgame> findAllByBggIdNull();

    List<Boardgame> findAllBy(TextCriteria criteria, Pageable pageable);

    List<Boardgame> findAllBy(Limit limit);
}
