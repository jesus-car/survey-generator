package com.surveygenerator.surveygenerator.document.infrastructure.output.database.mapper;

import com.surveygenerator.surveygenerator.document.domain.model.QuestionModel;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity.QuestionEntity;
import com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity.QuizEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class QuizEntityMapper {

    public QuizModel toModel(QuizEntity entity) {
        return QuizModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .statement(entity.getStatement())
                .questions(entity.getQuestions().stream()
                        .map(this::toQuestionModel)
                        .toList())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public QuizEntity toEntity(QuizModel model) {
        return QuizEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .statement(model.getStatement())
                .questions(model.getQuestions().stream()
                        .map(this::toQuestionEntity)
                        .toList())
                .createdAt(model.getCreatedAt() != null ? model.getCreatedAt() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private QuestionModel toQuestionModel(QuestionEntity entity) {
        return QuestionModel.builder()
                .question(entity.getQuestion())
                .options(entity.getOptions())
                .answer(entity.getAnswer())
                .build();
    }

    private QuestionEntity toQuestionEntity(QuestionModel model) {
        return QuestionEntity.builder()
                .question(model.getQuestion())
                .options(model.getOptions())
                .answer(model.getAnswer())
                .build();
    }
}
