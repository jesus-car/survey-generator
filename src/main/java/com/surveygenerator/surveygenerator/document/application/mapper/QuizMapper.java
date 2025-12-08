package com.surveygenerator.surveygenerator.document.application.mapper;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.domain.model.QuestionModel;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class QuizMapper {

    // Convierte QuizResponse (de la IA) a QuizModel para persistir
    public QuizModel toModel(QuizResponse response, String userId) {
        return QuizModel.builder()
                .userId(userId)
                .statement(response.statement())
                .questions(response.questions().stream()
                        .map(this::toQuestionModel)
                        .toList())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Convierte QuizModel (de DB) a QuizResponse para enviar al frontend
    public QuizResponse toResponse(QuizModel model) {
        return QuizResponse.builder()
                .statement(model.getStatement())
                .questions(model.getQuestions().stream()
                        .map(this::toQuestionResponse)
                        .toList())
                .build();
    }

    private QuestionModel toQuestionModel(QuizResponse.Question question) {
        return QuestionModel.builder()
                .question(question.question())
                .options(question.options().options())
                .answer(question.options().answer())
                .build();
    }

    private QuizResponse.Question toQuestionResponse(QuestionModel model) {
        return new QuizResponse.Question(
                model.getQuestion(),
                new QuizResponse.Question.QuestionOptions(
                        model.getOptions(),
                        model.getAnswer()
                )
        );
    }
}
