package com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "quizzes")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizEntity {
    @Id
    private String id;

    @Indexed  // Índice para búsquedas por usuario
    private String userId;

    private String statement;
    private List<QuestionEntity> questions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
