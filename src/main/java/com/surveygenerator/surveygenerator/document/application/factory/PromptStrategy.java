package com.surveygenerator.surveygenerator.document.application.factory;

import com.surveygenerator.surveygenerator.document.infrastructure.output.ai.dto.SurveyPromptOptions;
import org.springframework.ai.chat.prompt.Prompt;

public interface PromptStrategy {
    Prompt generatePrompt(SurveyPromptOptions promptOptions);
    String getStep();
}
