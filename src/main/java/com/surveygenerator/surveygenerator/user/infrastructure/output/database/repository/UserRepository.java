package com.surveygenerator.surveygenerator.user.infrastructure.output.database.repository;

import com.surveygenerator.surveygenerator.user.infrastructure.output.database.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}