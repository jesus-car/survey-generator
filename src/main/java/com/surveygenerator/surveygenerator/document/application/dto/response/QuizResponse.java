package com.surveygenerator.surveygenerator.document.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

import java.util.List;

@JsonPropertyOrder({"statement", "questions"})
@Builder
public record QuizResponse(
        @JsonProperty(required = true, value = "statement") String statement,
        @JsonProperty(required = true, value = "questions") List<Question> questions
){
    @JsonPropertyOrder({"question", "options"})
    public record Question(
            @JsonProperty(required = true, value = "question") String question,
            QuestionOptions options
    ) {
        @JsonPropertyOrder({"options", "answer"})
        public record QuestionOptions(
                @JsonProperty(required = true, value = "options") List<String> options,
                @JsonProperty(required = true, value = "answer") String answer
        ) {
        }
    }
}