package com.surveygenerator.surveygenerator.document.application.port.output;

import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;

import java.util.List;
import java.util.Optional;

public interface QuizAccessDatabasePort {

    QuizModel save(QuizModel quizModel);

    List<QuizModel> findAllByUserId(String userId);

    Optional<QuizModel> findByIdAndUserId(String id, String userId);
}
