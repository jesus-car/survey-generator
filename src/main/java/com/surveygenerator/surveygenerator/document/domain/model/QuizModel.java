package com.surveygenerator.surveygenerator.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizModel {
    private String id;
    private String userId;  // Referencia al usuario propietario
    private String statement;  // Título/enunciado del quiz
    private List<QuestionModel> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
