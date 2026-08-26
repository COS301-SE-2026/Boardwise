<<<<<<<< HEAD:backend/src/main/java/com/boardwise/backend/user_service/repository/BoardGameRepository.java
package com.boardwise.backend.user_service.repository;
========
package com.boardwise.backend.shared.repository;
>>>>>>>> 8f4054c6958c66fbb30aeb0c75014dee8a3cea30:backend/src/main/java/com/boardwise/backend/shared/repository/BoardGameRepository.java

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.shared.model.Boardgame;

public interface BoardGameRepository extends MongoRepository<Boardgame, String>{
    Optional<Boardgame> findByTitle(String title);

    Optional<Boardgame> findTopByBggIdNotNullOrderByBggIdDesc();

    List<Boardgame> findAllByBggIdNull();

    List<Boardgame> findAllBy(TextCriteria criteria, Pageable pageable);

    List<Boardgame> findAllBy(Limit limit);

    List<Boardgame> findByGenresIn(List<String> genres, Limit limit);


}
