package com.surveygenerator.surveygenerator.document.infrastructure.output.database.repository;

import com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity.QuizEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends MongoRepository<QuizEntity, String> {

    // Buscar todos los quizzes de un usuario, ordenados por fecha (más recientes primero)
    List<QuizEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    // Buscar un quiz específico que pertenezca a un usuario (para seguridad)
    Optional<QuizEntity> findByIdAndUserId(String id, String userId);
}
