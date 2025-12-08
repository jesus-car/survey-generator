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

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class OllamaProcessorAdapter implements AiProcessorPort {

//    @Value("classpath:/static/prompt/GenerateQuizPrompt")
//    private Resource generateQuizPrompt;
//
//    private final ChatClient chatClient;
//
//    @Override
//    public QuizResponse generateQuestions(String markdownContent) {
//        log.info("Generating questions from markdown content");
//
//        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<QuizResponse>(){});
//
//        String response = chatClient.prompt()
//                .user(u -> u.text(generateQuizPrompt)
//                        .param( "format", converter.getFormat())
//                        .param("document", markdownContent)
//                        .param("q", "5") // Number of questions to generate
//                        .param("o", "5")) // Number of options per question
//                .call()
//                .content();
//
//        // Sanitize response to remove control characters that cause JSON parsing issues
//        String sanitizedResponse = sanitizeJsonResponse(response);
//        log.debug("Sanitized response: {}", sanitizedResponse);
//
//        return converter.convert(sanitizedResponse);
//    }
//
//    /**
//     * Sanitizes the LLM response by removing or replacing control characters
//     * that can cause JSON parsing errors.
//     *
//     * @param response the raw response from the LLM
//     * @return sanitized response with control characters handled
//     */
//    private String sanitizeJsonResponse(String response) {
//        if (response == null) {
//            return null;
//        }
//
//        // Replace tabs with spaces to avoid "Illegal unquoted character" errors
//        String sanitized = response.replace("\t", "    ");
//
//        // Remove other problematic control characters (except \n and \r which are valid in JSON strings when escaped)
//        // This removes control characters in the range 0x00-0x1F except newline (0x0A) and carriage return (0x0D)
//        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
//
//        return sanitized;
//    }
//}