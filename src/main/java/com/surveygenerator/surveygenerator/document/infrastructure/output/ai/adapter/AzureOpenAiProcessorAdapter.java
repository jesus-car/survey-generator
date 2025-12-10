package com.surveygenerator.surveygenerator.document.infrastructure.output.ai.adapter;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.application.port.output.AiProcessorPort;
import com.surveygenerator.surveygenerator.document.infrastructure.output.ai.strategy.DefaultPrompt;
import com.surveygenerator.surveygenerator.document.infrastructure.output.ai.dto.SurveyPromptOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AzureOpenAiProcessorAdapter implements AiProcessorPort {

    private final AzureOpenAiChatModel azureOpenAiChatModel;
    private final DefaultPrompt promptFactory;

    @Override
    public QuizResponse generateQuestions(String markdownContent) {
        var outputConverter = new BeanOutputConverter<>(QuizResponse.class);

        SurveyPromptOptions surveyPromptOptions = SurveyPromptOptions.builder()
                .numberOfOptionsPerQuestion(5)
                .numberOfQuestions(10)
                .markdownContent(markdownContent)
                .format(outputConverter.getFormat())
                .build();

        Prompt prompt = promptFactory.generatePrompt(surveyPromptOptions);

        log.info("Generating questions from markdown content using Azure OpenAI");
        ChatResponse response = azureOpenAiChatModel
                .call(prompt);

        String responseContent = response.getResult().getOutput().getText();
        log.info("Azure OpenAI response successfully received");

        return outputConverter.convert(responseContent);
    }
}
