package com.surveygenerator.surveygenerator.document.infrastructure.output.database.adapter;

import com.surveygenerator.surveygenerator.document.application.port.output.QuizAccessDatabasePort;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import com.surveygenerator.surveygenerator.document.infrastructure.output.database.mapper.QuizEntityMapper;
import com.surveygenerator.surveygenerator.document.infrastructure.output.database.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuizAccessDatabaseAdapter implements QuizAccessDatabasePort {

    private final QuizRepository quizRepository;
    private final QuizEntityMapper quizEntityMapper;

    @Override
    public QuizModel save(QuizModel quizModel) {
        var entity = quizEntityMapper.toEntity(quizModel);
        var savedEntity = quizRepository.save(entity);
        return quizEntityMapper.toModel(savedEntity);
    }

    @Override
    public List<QuizModel> findAllByUserId(String userId) {
        return quizRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(quizEntityMapper::toModel)
                .toList();
    }

    @Override
    public Optional<QuizModel> findByIdAndUserId(String id, String userId) {
        return quizRepository.findByIdAndUserId(id, userId)
                .map(quizEntityMapper::toModel);
    }
}
