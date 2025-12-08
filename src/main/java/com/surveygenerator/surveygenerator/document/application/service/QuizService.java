package com.surveygenerator.surveygenerator.document.application.service;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizHistoryResponse;
import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.application.mapper.QuizMapper;
import com.surveygenerator.surveygenerator.document.application.port.output.QuizAccessDatabasePort;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizAccessDatabasePort quizAccessDatabasePort;
    private final QuizMapper quizMapper;

    public List<QuizHistoryResponse> getUserQuizHistory(String userId) {
        return quizAccessDatabasePort.findAllByUserId(userId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public QuizResponse getQuizById(String quizId, String userId) {
        QuizModel quizModel = quizAccessDatabasePort.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found or does not belong to user"));

        return quizMapper.toResponse(quizModel);
    }

    private QuizHistoryResponse toHistoryResponse(QuizModel model) {
        return QuizHistoryResponse.builder()
                .id(model.getId())
                .statement(model.getStatement())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
