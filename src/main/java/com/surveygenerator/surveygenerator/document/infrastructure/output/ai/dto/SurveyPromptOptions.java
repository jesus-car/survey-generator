package com.surveygenerator.surveygenerator.document.infrastructure.output.ai.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SurveyPromptOptions {
    private int numberOfQuestions;
    private int numberOfOptionsPerQuestion;
    private String markdownContent;
    private String format;
}
