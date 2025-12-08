package com.surveygenerator.surveygenerator.document.application.service;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.application.mapper.QuizMapper;
import com.surveygenerator.surveygenerator.document.application.port.output.AiProcessorPort;
import com.surveygenerator.surveygenerator.document.application.port.output.QuizAccessDatabasePort;
import com.surveygenerator.surveygenerator.document.domain.model.QuizModel;
import com.surveygenerator.surveygenerator.utils.ValidatorResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final FileContentProcessor fileContentProcessor;
    private final AiProcessorPort aiProcessorPort;
    private final QuizAccessDatabasePort quizAccessDatabasePort;
    private final QuizMapper quizMapper;

    public List<QuizResponse> generateQuestions(List<MultipartFile> files, String userId) {
        return files.stream()
                .map(fileContentProcessor::extractContent)
                .map(this::validateContent)
                .map(aiProcessorPort::generateQuestions)
                .map(response -> saveQuiz(response, userId))
                .toList();
    }

//    public QuizResponse generateQuestionsOfUser(FilePart file, FormFieldPart userIdPart) {
//        return fileContentProcessor.extractContent(file)
//                .flatMap(this::validateContent)
//                .flatMap(questionGenerationProcessor::generateQuestions)
//                .flatMap(quizDTO -> {
//                    QuizDocument quizDocument = QuizDocument.builder()
//                            .statement(quizDTO.statement())
//                            .userId(userIdPart.value())
//                            .questions(convertToQuestionDocuments(quizDTO.questions()))
//                            .build();
//                    return quizRespository.save(quizDocument)
//                            .thenReturn(quizDTO);
//                });
//    }



    private String validateContent(String content) {
        ValidatorResult<String> contentValidation = fileContentProcessor.validateContent(content);

        if (!contentValidation.isValid()) {
            throw new IllegalArgumentException(contentValidation.getErrorMessage());
        }

        return contentValidation.getData();
    }

    private QuizResponse saveQuiz(QuizResponse response, String userId) {
        // Only save to database if user is authenticated
        if (userId != null) {
            log.info("Saving generated quiz for userId: {}", userId);
            QuizModel quizModel = quizMapper.toModel(response, userId);
            quizAccessDatabasePort.save(quizModel);
        } else {
            log.debug("Skipping database save for anonymous user");
        }
        return response;
    }

//    private List<QuestionDocument> convertToQuestionDocuments(List<QuizDTO.Question> questions) {
//        return questions.stream()
//                .map(question -> QuestionDocument.builder()
//                        .statement(question.question())
//                        .options(question.options().options())
//                        .answer(question.options().answer())
//                        .build())
//                .toList();
//    }
}
