package com.surveygenerator.surveygenerator.document.infrastructure.output.ai.adapter;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.application.port.output.AiProcessorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiProcessorAdapter implements AiProcessorPort {

    @Value("classpath:/static/prompt/GenerateQuizPrompt")
    private Resource generateQuizPrompt;

    private final ChatClient chatClient;

    @Override
    public QuizResponse generateQuestions(String markdownContent) {
        log.info("Generating questions from markdown content");

        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<QuizResponse>(){});

        String response = chatClient.prompt()
                .user(u -> u.text(generateQuizPrompt)
                        .param( "format", converter.getFormat())
                        .param("document", markdownContent)
                        .param("q", "5") // Number of questions to generate
                        .param("o", "5")) // Number of options per question
                .call()
                .content();

        return converter.convert(response);
    }
}