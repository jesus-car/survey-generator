package com.surveygenerator.surveygenerator.document.application.port.output;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;

public interface AiProcessorPort {

    QuizResponse generateQuestions(String markdownContent);
}
