package com.boardwise.backend.shared.repository;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.shared.model.BoardgameStatsSnapshot;

public interface BoardgameStatsSnapshotRepository extends MongoRepository<BoardgameStatsSnapshot, String> {

    List<BoardgameStatsSnapshot> findByBggIdOrderByCapturedAtDesc(Integer bggId, Limit limit);
}