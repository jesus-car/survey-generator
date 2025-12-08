package com.surveygenerator.surveygenerator.document.infrastructure.output.ai.strategy;


import com.surveygenerator.surveygenerator.document.application.factory.PromptStrategy;
import com.surveygenerator.surveygenerator.document.infrastructure.output.ai.dto.SurveyPromptOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DefaultPrompt implements PromptStrategy {

    @Value("classpath:/static/prompt/GenerateQuizPrompt")
    private Resource generateQuizPrompt;

    @Value("classpath:/static/prompt/ExpertSystemPrompt")
    private Resource proSystemPrompt;

    public Prompt generatePrompt(SurveyPromptOptions promptOptions) {

        AzureOpenAiChatOptions azureOpenAiChatOptions =
                AzureOpenAiChatOptions.builder()
                        .deploymentName("gpt-5-nano")
                .build();

        PromptTemplate promptTemplate = new PromptTemplate(generateQuizPrompt);
        Map<String, Object> promptParams = Map.of(
                "format", promptOptions.getFormat(),
                "document", promptOptions.getMarkdownContent(),
                "q", promptOptions.getNumberOfQuestions(),
                "o", promptOptions.getNumberOfOptionsPerQuestion()
        );

        Message userMessage = promptTemplate.createMessage(promptParams);
        SystemMessage systemMessage = new SystemMessage(proSystemPrompt);

        log.info("Prompt generated successfully");

        return new Prompt(List.of(systemMessage, userMessage), azureOpenAiChatOptions);
    }

    @Override
    public String getStep() {
        return "DEFAULT";
    }
}
