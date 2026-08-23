package com.boardwise.backend.user_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.Group;
import java.util.List;


public interface GroupRepository extends MongoRepository<Group, String>{
    List<Group> findByName(String name);
}
